package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.output;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class OutputGlyph extends GlyphAsset {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Output";

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
