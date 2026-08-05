package com.riprod.hexcode.core.common.execution.queue;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexQueueDrainEvent;

public class HexExecutionTickSystem extends TickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        int pending = queue.size();
        if (pending == 0) {
            return;
        }
        queue.nextTick();
        store.invoke(new HexQueueDrainEvent(pending));
    }
}
