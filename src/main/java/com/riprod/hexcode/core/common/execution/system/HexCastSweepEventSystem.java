package com.riprod.hexcode.core.common.execution.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CastEndEvent;
import com.riprod.hexcode.api.event.HexCastSweepEvent;
import com.riprod.hexcode.api.execution.HexExecuter;

public class HexCastSweepEventSystem extends WorldEventSystem<EntityStore, HexCastSweepEvent> {

    public HexCastSweepEventSystem() {
        super(HexCastSweepEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexCastSweepEvent event) {
        for (var cast : event.getFinished()) {
            HexExecuter.endCast(cast, buffer);
        }
        for (var cast : event.getOverCap()) {
            HexExecuter.endCast(cast, CastEndEvent.Reason.EVICTED, buffer);
        }
    }
}
