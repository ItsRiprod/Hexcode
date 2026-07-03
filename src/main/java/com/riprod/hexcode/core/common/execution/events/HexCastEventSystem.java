package com.riprod.hexcode.core.common.execution.events;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.CoreHexExecuter;

public class HexCastEventSystem extends WorldEventSystem<EntityStore, HexCastEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexCastEventSystem() {
        super(HexCastEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> buffer,
                       @Nonnull HexCastEvent event) {
        if (event.isCancelled()) return;
        CoreHexExecuter.runPostGate(event.getContext(), buffer);
    }
}
