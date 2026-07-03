package com.riprod.hexcode.builtin.hexCore.glyphs.elementfire;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class ElementFireGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String ID = "ElementFire";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public float getComplexity(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        float power = hexContext.consumeComplexity();
        LOGGER.atInfo().log("ElementFire consumes %.2f complexity as fire damage", power);
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
