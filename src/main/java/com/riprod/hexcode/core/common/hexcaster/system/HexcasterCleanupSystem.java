package com.riprod.hexcode.core.common.hexcaster.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;

public class HexcasterCleanupSystem extends RefSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return HexcasterComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {

        try {
            HexcasterComponent comp = store.getComponent(ref, HexcasterComponent.getComponentType());
            if (comp == null)
                return;

            ContextTransitionService.setInContextStat(buffer, ref, false);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexcasterCleanupSystem.onEntityAdded failed: %s", e.getMessage());
        }

    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            HexcasterComponent comp = store.getComponent(ref, HexcasterComponent.getComponentType());
            if (comp == null)
                return;

        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexcasterCleanupSystem.onEntityRemove failed: %s", e.getMessage());
        }
    }
}
