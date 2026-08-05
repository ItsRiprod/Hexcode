package com.riprod.hexcode.builtin.counterspell.eventListeners;

import java.util.function.Consumer;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.HexStateChangeEvent;
import com.riprod.hexcode.utils.LogScopes;

public class HexStateDiagnosticListener implements Consumer<HexStateChangeEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    @Override
    public void accept(HexStateChangeEvent event) {
        LOGGER.atFine().log("[state] player=%s %s -> %s",
                event.getPlayerRef(),
                event.getPreviousState(),
                event.getNewState());
    }
}
