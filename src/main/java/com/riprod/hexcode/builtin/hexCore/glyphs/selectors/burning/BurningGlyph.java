package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.burning;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class BurningGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Burning";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.NOT_IMPLEMENTED);
    }
}
