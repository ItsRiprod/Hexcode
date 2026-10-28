package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.pi;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class PiValue extends GlyphAsset {
    public static final String TYPE = "Pi";

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        return new NumberVar(Math.PI);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
