package com.riprod.hexcode.core.common.appearance;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HexAppearanceRevertSystem extends RefSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return HexAppearanceComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            HexAppearanceComponent appearance = store.getComponent(ref, HexAppearanceComponent.getComponentType());
            if (appearance == null || appearance.hasLayers()) return;

            if (store.getComponent(ref, PersistentModel.getComponentType()) == null) {
                buffer.tryRemoveComponent(ref, HexAppearanceComponent.getComponentType());
                return;
            }

            LOGGER.atInfo().log("[hexcode] appearance: reverting orphaned model on load");
            HexAppearanceService.restoreOriginal(buffer, ref);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexAppearanceRevertSystem.onEntityAdded failed: %s", e.getMessage());
        }
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
    }
}
