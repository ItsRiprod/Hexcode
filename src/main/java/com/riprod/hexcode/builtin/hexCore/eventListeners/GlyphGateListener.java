package com.riprod.hexcode.builtin.hexCore.eventListeners;

import java.util.Set;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexQueueDrainEvent;
import com.riprod.hexcode.core.common.execution.gate.GateStateResource;
import com.riprod.hexcode.core.common.execution.queue.HexQueueDrainEventSystem;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;

public class GlyphGateListener extends WorldEventSystem<EntityStore, HexQueueDrainEvent> {

    public GlyphGateListener() {
        super(HexQueueDrainEvent.class);
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency<>(Order.BEFORE, HexQueueDrainEventSystem.class));
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexQueueDrainEvent event) {
        GateStateResource gate = store.getResource(GateStateResource.getResourceType());
        long now = store.getResource(TimeResource.getResourceType()).getNow().toEpochMilli();
        if (gate.isGloballyGated(now)) {
            store.getResource(HexExecutionQueue.getResourceType()).clear();
        }
    }
}
