package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.common.ContextForceExitEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;

public class CraftingForceExitSystem extends EntityEventSystem<EntityStore, ContextForceExitEvent> {

    public CraftingForceExitSystem() {
        super(ContextForceExitEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ContextForceExitEvent event) {
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        ContextTransitionService.exit(buffer, player, CraftingState.CONTEXT_ID);
    }
}
