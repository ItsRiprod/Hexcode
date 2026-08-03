package com.riprod.hexcode.builtin.hexCore.contexts.selecting.system;

import java.util.List;

import javax.annotation.Nonnull;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.builtin.hexCore.pedestals.PedestalSceneHover;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.node.NodeRouter;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class SelectingTickSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return SelectingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
            if (pedestal == null) {
                ContextTransitionService.exit(buffer, player, SelectingState.CONTEXT_ID);
                return;
            }
            HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
            if (session == null) {
                ContextTransitionService.exit(buffer, player, SelectingState.CONTEXT_ID);
                return;
            }

            if (drainPendingReenter(buffer, player, session)) {
                return;
            }

            PedestalSceneHover.tick(buffer, dt, player, pedestal, session);

            if (outsideRadius(buffer, player, pedestal)) {
                ContextTransitionService.exit(buffer, player, SelectingState.CONTEXT_ID);
                return;
            }

            CasterComponent caster = chunk.getComponent(index, CasterComponent.getComponentType());
            if (caster != null) {
                if (caster.consumePrimaryPressed()) {
                    enterInteraction(player, buffer);
                }
                caster.consumePrimaryReleased();
                caster.consumeAbilityPressed();
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] selecting tick failed");
        }
    }

    private static void enterInteraction(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer) {
        HexcasterCraftingComponent craftingComp = buffer.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null)
            return;

        Ref<EntityStore> hoveredRef = craftingComp.getHoveredRef();
        if (hoveredRef == null || !hoveredRef.isValid())
            return;

        craftingComp.setDragTickCount(0);

        NodeRouter.enter(buffer, hoveredRef, ref);
    }

    private static boolean drainPendingReenter(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            HexcodeSessionComponent session) {

        String pendingKey = session.getPendingReenterSlotKey();
        if (pendingKey == null)
            return false;
        if (session.getState() != PedestalState.SELECTING)
            return false;

        Ref<EntityStore> previewRef = findPreviewBySlotKey(buffer, session, pendingKey);
        if (previewRef == null || !previewRef.isValid())
            return false;

        session.setPendingReenterSlotKey(null);
        NodeRouter.enter(buffer, previewRef, player);
        return true;
    }

    private static Ref<EntityStore> findPreviewBySlotKey(CommandBuffer<EntityStore> buffer,
            HexcodeSessionComponent session, String slotKey) {

        List<Ref<EntityStore>> previews = session.getHexPreviewRefs();
        if (previews == null)
            return null;
        for (Ref<EntityStore> ref : previews) {
            if (ref == null || !ref.isValid())
                continue;
            SlotComponent slotRef = buffer.getComponent(ref,
                    SlotComponent.getComponentType());
            if (slotRef != null && slotKey.equals(slotRef.getSlotKey())) {
                return ref;
            }
        }
        return null;
    }

    private static boolean outsideRadius(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            PedestalBlockComponent pedestal) {
        Vector3i pedestalLoc = pedestal.getLocation();
        TransformComponent transform = buffer.getComponent(player, TransformComponent.getComponentType());
        if (pedestalLoc == null || transform == null || transform.getPosition() == null) {
            return false;
        }
        Vector3d center = new Vector3d(
                pedestalLoc.x() + 0.5,
                pedestalLoc.y() + 0.5,
                pedestalLoc.z() + 0.5);
        double maxRadius = pedestal.getMaxRadius();
        return transform.getPosition().distanceSquared(center) > maxRadius * maxRadius;
    }
}
