package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.burning;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;

public class BurningGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String ID = "Burning";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.NOT_IMPLEMENTED);
    }
}
