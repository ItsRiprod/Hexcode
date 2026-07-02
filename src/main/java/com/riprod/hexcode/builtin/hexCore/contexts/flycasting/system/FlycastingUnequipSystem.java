package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryActiveSlotRequestEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;

public class FlycastingUnequipSystem extends EntityEventSystem<EntityStore, InventoryActiveSlotRequestEvent> {

    public FlycastingUnequipSystem() {
        super(InventoryActiveSlotRequestEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return FlycastingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull InventoryActiveSlotRequestEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        ContextTransitionService.exit(buffer, player, FlycastingState.CONTEXT_ID);
    }
}
