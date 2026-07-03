package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.utils.CleanupUtils;

public class CraftingCleanupSystem extends RefSystem<EntityStore> {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        HexcasterCraftingComponent craftingComp = store.getComponent(ref,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            CleanupUtils.safeRemoveEntity(buffer, craftingComp.getHeadAnchorRef());
            craftingComp.setHeadAnchorRef(null);
        }
    }
}
