package com.riprod.hexcode.builtin.counterspell.eventListeners;

import java.util.Arrays;
import java.util.function.Consumer;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.GlyphExecuteEvent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.cast.VolatilityComponent;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.utils.LogScopes;

public class GlyphExecuteDiagnosticListener implements Consumer<GlyphExecuteEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    @Override
    public void accept(GlyphExecuteEvent event) {
        Glyph glyph = event.getGlyph();
        HexContext ctx = event.getCtx();
        VolatilityComponent stats = ctx != null ? ctx.volatility() : null;
        Slot next = glyph != null ? glyph.getSlot(Glyph.NEXT_SLOT) : null;
        LOGGER.atFine().log("[exec] %s(%s) next=%s vol=%s",
                event.getNodeId(),
                glyph != null ? glyph.getGlyphId() : "<null>",
                next != null ? Arrays.toString(next.getLinks()) : "[]",
                stats != null ? stats.getCurrent() : "<null>");
    }
}
