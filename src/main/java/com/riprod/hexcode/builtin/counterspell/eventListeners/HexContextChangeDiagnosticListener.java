package com.riprod.hexcode.builtin.counterspell.eventListeners;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.HexContextChangeEvent;

public class HexContextChangeDiagnosticListener extends WorldEventSystem<EntityStore, HexContextChangeEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexContextChangeDiagnosticListener() {
        super(HexContextChangeEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContextChangeEvent event) {
        LOGGER.atInfo().log("[event] HexContextChange player=%s newContext=%s",
                event.getPlayer(), event.getNewContextId());
    }
}
