package com.riprod.hexcode.core.common.drawing;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryActiveSlotRequestEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.state.HexState;

public class DrawingSlotLockEvent extends EntityEventSystem<EntityStore, InventoryActiveSlotRequestEvent> {

    public DrawingSlotLockEvent() {
        super(InventoryActiveSlotRequestEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull InventoryActiveSlotRequestEvent event) {
        HexcasterComponent hexcaster = chunk.getComponent(index, HexcasterComponent.getComponentType());
        if (hexcaster == null) {
            return;
        }
        if (hexcaster.getState() == HexState.DRAWING) {
            event.setCancelled(true);
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return HexcasterComponent.getComponentType();
    }
}
