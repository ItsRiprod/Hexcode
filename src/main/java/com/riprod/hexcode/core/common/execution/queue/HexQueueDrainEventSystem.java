package com.riprod.hexcode.core.common.execution.queue;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexQueueDrainEvent;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue.PendingGlyph;

public class HexQueueDrainEventSystem extends WorldEventSystem<EntityStore, HexQueueDrainEvent> {

    public HexQueueDrainEventSystem() {
        super(HexQueueDrainEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexQueueDrainEvent event) {
        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        for (int i = 0; i < event.getCount(); i++) {
            PendingGlyph item = queue.poll();
            if (item == null) {
                break;
            }
            HexContext ctx = item.ctx();
            ctx.UpdateAccessor(buffer);
            CoreHexExecuter.executeQueuedGlyph(item.glyphId(), ctx);
        }
    }
}
