package com.riprod.hexcode.builtin.counterspell.eventListeners;

import java.util.Arrays;
import java.util.function.Consumer;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.GlyphEvent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.utils.LogScopes;

public class GlyphExecuteDiagnosticListener implements Consumer<GlyphEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    @Override
    public void accept(GlyphEvent event) {
        Glyph glyph = event.getGlyph();
        Slot next = glyph.getSlot(Glyph.NEXT_SLOT);
        LOGGER.atFine().log("[exec] %s(%s) next=%s vol=%s resolvedCost=%s cancelled=%s",
                event.getNodeId(),
                event.getAssetId(),
                next != null ? Arrays.toString(next.getLinks()) : "[]",
                event.getContext().volatility().getCurrent(),
                event.getResolvedCost(),
                event.isCancelled());
    }
}
