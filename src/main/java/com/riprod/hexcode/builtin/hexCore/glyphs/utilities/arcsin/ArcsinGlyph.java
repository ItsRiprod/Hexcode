package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.arcsin;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class ArcsinGlyph extends GlyphAsset {
    public static final String TYPE = "Arcsin";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        var a = glyph.readSlot(ArcsinGlyphSlots.A, hexContext);
        if (a == null)
            return null;
        var s = a.scalar();
        if (s == null)
            return null;
        return new NumberVar(Math.asin(s));
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        var self = hexContext.getOwnVariable(glyph.getId());

        if (self != null) {
            return self;
        }
        return compute(glyph, hexContext);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var result = compute(glyph, hexContext);

        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
