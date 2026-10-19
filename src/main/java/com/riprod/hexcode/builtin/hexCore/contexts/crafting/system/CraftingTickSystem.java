package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.pedestals.PedestalSceneHover;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;

public class CraftingTickSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            HexcasterCraftingComponent craftingComp = chunk.getComponent(index,
                    HexcasterCraftingComponent.getComponentType());

            if (craftingComp != null) {
                drainAnchors(buffer, craftingComp);
            }

            PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
            if (pedestal == null) {
                ContextTransitionService.exit(buffer, player, CraftingState.CONTEXT_ID);
                return;
            }

            if (PedestalBlockUtil.outsideRadius(buffer, player, pedestal)) {
                ContextTransitionService.exit(buffer, player, CraftingState.CONTEXT_ID);
                return;
            }

            PedestalSceneHover.tick(buffer, dt, player, pedestal);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] crafting tick failed");
        }
    }

    private static void drainAnchors(CommandBuffer<EntityStore> buffer,
            HexcasterCraftingComponent craftingComp) {
        if (craftingComp.getDraggingRef() == null
                && craftingComp.getHeadAnchorRef() != null
                && craftingComp.getHeadAnchorRef().isValid()) {
            buffer.tryRemoveComponent(craftingComp.getHeadAnchorRef(), MountedComponent.getComponentType());
            buffer.tryRemoveEntity(craftingComp.getHeadAnchorRef(), RemoveReason.REMOVE);
            craftingComp.setHeadAnchorRef(buffer, null);
        }

        List<Ref<EntityStore>> pending = craftingComp.getPendingDespawn();
        for (int i = pending.size() - 1; i >= 0; i--) {
            Ref<EntityStore> pendingRef = pending.get(i);
            if (pendingRef == null) {
                pending.remove(i);
                continue;
            }
            if (pendingRef.isValid()) {
                buffer.tryRemoveComponent(pendingRef, MountedComponent.getComponentType());
                buffer.tryRemoveEntity(pendingRef, RemoveReason.REMOVE);
                pending.remove(i);
            }
        }
    }
}
