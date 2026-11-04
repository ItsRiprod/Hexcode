package com.riprod.hexcode.core.common.execution.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastSweepEvent;
import com.riprod.hexcode.api.event.HexQueueDrainEvent;
import com.riprod.hexcode.config.HexcodeConfig;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue;
import com.riprod.hexcode.utils.LogScopes;

public class HexExecutionTickSystem extends TickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        sweepCasts(store);

        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        int pending = queue.size();
        if (pending == 0) {
            return;
        }
        queue.nextTick();
        store.invoke(new HexQueueDrainEvent(pending));
    }

    private static void sweepCasts(@Nonnull Store<EntityStore> store) {
        var casts = store.getResource(HexCastStore.getResourceType());
        var finished = casts.collectFinished();

        var cap = HexcodeConfig.get().getMaxActiveCastsPerWorld();
        var overCap = casts.collectOverCap(cap);
        if (!overCap.isEmpty()) {
            LOGGER.atWarning().log("world exceeded its %d live spell cap; ending the %d oldest",
                    cap, overCap.size());
        }

        if (finished.isEmpty() && overCap.isEmpty()) {
            return;
        }
        store.invoke(new HexCastSweepEvent(finished, overCap));
    }
}
