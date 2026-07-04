package com.riprod.hexcode.builtin.counterspell.eventListeners;

import java.util.Arrays;
import java.util.function.Consumer;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.GlyphExecuteEvent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;

public class GlyphExecuteDiagnosticListener implements Consumer<GlyphExecuteEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void accept(GlyphExecuteEvent event) {
        Glyph glyph = event.getGlyph();
        HexContext ctx = event.getCtx();
        HexStats stats = ctx != null ? ctx.getHexStats() : null;
        Slot next = glyph != null ? glyph.getSlot(Glyph.NEXT_SLOT) : null;
        LOGGER.atInfo().log("[exec] %s(%s) next=%s vol=%s",
                event.getNodeId(),
                glyph != null ? glyph.getGlyphId() : "<null>",
                next != null ? Arrays.toString(next.getLinks()) : "[]",
                stats != null ? stats.getCurrentVolatility() : "<null>");
    }
}
