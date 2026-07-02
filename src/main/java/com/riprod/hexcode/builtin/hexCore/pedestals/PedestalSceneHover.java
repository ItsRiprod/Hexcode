package com.riprod.hexcode.builtin.hexCore.pedestals;

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
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.core.state.crafting.utils.HoverStyleUtils;
import com.riprod.hexcode.core.state.crafting.utils.LinkRenderer;

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

            HoverStyleUtils.unhover(buffer, previousHovered, player);

            craftingComp.setHoveredRef(targetRef);
            pedestal.setTickLength(HOVER_PARTICLE, 1f);

            HoverStyleUtils.hover(buffer, targetRef, player);
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

        HoverStyleUtils.hoverParticles(buffer, craftingComp.getHoveredRef(), dt, pedestal);

        HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);

        if (session != null) {
            LinkRenderer.renderLinks(buffer, session, pedestal, dt);
        }
    }
}
