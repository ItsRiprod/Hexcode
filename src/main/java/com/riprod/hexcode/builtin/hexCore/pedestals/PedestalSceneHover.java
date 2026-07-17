package com.riprod.hexcode.builtin.hexCore.pedestals;

import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.core.common.hover.component.HoverableComponent;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import org.joml.Vector3d;
import java.util.List;
import java.util.Objects;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HoverChangeEvent;
import com.riprod.hexcode.core.common.hover.utils.HoverableUtils;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.builtin.hexCore.scene.LinkRenderer;

public final class PedestalSceneHover {

    private static final String HOVER_PARTICLE = "Object_Hover";

    private PedestalSceneHover() {
    }

    public static void tick(CommandBuffer<EntityStore> buffer, float dt, Ref<EntityStore> player,
            PedestalBlockComponent pedestal) {
        HexcasterCraftingComponent craftingComp = buffer.getComponent(player,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return;

        TransformComponent playerTransform = buffer.getComponent(player,
                TransformComponent.getComponentType());
        if (playerTransform == null)
            return;

        List<Ref<EntityStore>> nearby = HoverableUtils.getNearbyHoverables(buffer,
                playerTransform.getPosition(), 8.0);

        Ref<EntityStore> draggedRef = craftingComp.getDraggingRef();
        if (draggedRef != null && draggedRef.isValid()) {
            nearby.remove(draggedRef); // remove the dragged ref from the hovered list
        }

        Ref<EntityStore> targetRef = HoverableUtils.getSmallestTarget(buffer, player, nearby);

        Ref<EntityStore> previousHovered = craftingComp.getHoveredRef();
        boolean changed = !Objects.equals(targetRef, previousHovered);

        if (changed) {

            unhover(buffer, previousHovered, player);

            craftingComp.setHoveredRef(targetRef);
            pedestal.setTickLength(HOVER_PARTICLE, 1f);

            hover(buffer, targetRef, player);
            HytaleServer.get().getEventBus().dispatchFor(HoverChangeEvent.class)
                    .dispatch(new HoverChangeEvent(player, targetRef, previousHovered));

            PedestalBlockComponent ped = PedestalBlockUtil.resolvePedestal(player, buffer);
            if (ped != null) {
                if (previousHovered != null)
                    ObeliskDispatcher.dispatchUnhover(buffer, ped, player, previousHovered);
                if (targetRef != null)
                    ObeliskDispatcher.dispatchHover(buffer, ped, player, targetRef);
            }
        }

        hoverParticles(buffer, craftingComp.getHoveredRef(), dt, pedestal);

        HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);

        if (session != null) {
            LinkRenderer.renderLinks(buffer, session, pedestal, dt);
        }
    }

    private static void unhover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> unhoveredRef,
            Ref<EntityStore> playerRef) {
        if (unhoveredRef == null || !unhoveredRef.isValid())
            return;
        if (accessor.getComponent(unhoveredRef, HoverableComponent.getComponentType()) == null)
            return;
        NodeRouter.unhover(accessor, unhoveredRef, playerRef);
    }

    private static void hover(CommandBuffer<EntityStore> accessor, Ref<EntityStore> hovered,
            Ref<EntityStore> playerRef) {
        if (hovered == null || !hovered.isValid())
            return;
        if (accessor.getComponent(hovered, HoverableComponent.getComponentType()) == null)
            return;
        NodeRouter.hover(accessor, hovered, playerRef);
    }

    private static void hoverParticles(CommandBuffer<EntityStore> accessor, Ref<EntityStore> hovered,
            float dt, PedestalBlockComponent pedestal) {
        if (hovered == null || !hovered.isValid())
            return;
        if (accessor.getComponent(hovered, GlyphComponent.getComponentType()) == null)
            return;
        if (pedestal.getTickLength(HOVER_PARTICLE) > 0f) {
            pedestal.incrementTickLength(HOVER_PARTICLE, dt);
            return;
        }
        pedestal.setTickLength(HOVER_PARTICLE, -0.5f);
        TransformComponent transform = accessor.getComponent(hovered, TransformComponent.getComponentType());
        if (transform == null)
            return;
        Vector3d pos = transform.getPosition();
        ParticleUtil.spawnParticleEffect(HOVER_PARTICLE, pos, accessor);
    }
}
