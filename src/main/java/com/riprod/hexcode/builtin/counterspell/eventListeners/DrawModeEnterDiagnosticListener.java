package com.riprod.hexcode.builtin.counterspell.eventListeners;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeEnterEvent;
import com.riprod.hexcode.utils.LogScopes;

public class DrawModeEnterDiagnosticListener extends WorldEventSystem<EntityStore, DrawModeEnterEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    public DrawModeEnterDiagnosticListener() {
        super(DrawModeEnterEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull DrawModeEnterEvent event) {
        LOGGER.atFine().log("[event] DrawModeEnter player=%s palette=%d",
                event.getPlayer(), event.getPalette() != null ? event.getPalette().size() : 0);
    }
}
