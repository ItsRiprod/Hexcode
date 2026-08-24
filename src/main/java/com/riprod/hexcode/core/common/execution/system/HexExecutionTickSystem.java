package com.riprod.hexcode.core.common.execution.system;

import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexQueueDrainEvent;
import com.riprod.hexcode.config.HexcodeConfig;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
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
        HexCastStore casts = store.getResource(HexCastStore.getResourceType());
        casts.nextTick();
        casts.reclaimFinished();

        HexcodeConfig config = HexcodeConfig.get();
        int lifetime = config.getMaxCastLifetimeTicks();
        for (HexCast cast : casts.endExpired(lifetime)) {
            LOGGER.atWarning().log(
                    "spell %s outlived the %d tick limit with %d branch(es) still open; forced to end",
                    cast.getExecutionId(), lifetime, cast.getActiveBranchCount());
        }

        int cap = config.getMaxActiveCastsPerWorld();
        List<HexCast> overCap = casts.endOverCap(cap);
        if (!overCap.isEmpty()) {
            LOGGER.atWarning().log("world exceeded its %d live spell cap; ended the %d oldest",
                    cap, overCap.size());
        }
    }
}
