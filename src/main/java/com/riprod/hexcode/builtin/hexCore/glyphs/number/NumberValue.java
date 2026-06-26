package com.riprod.hexcode.builtin.hexCore.glyphs.number;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class NumberValue implements GlyphHandler {

    @Override
    public String getId() {
        return "Number_" + this.number;
    };

    private int number;

    public NumberValue() {
    }

    public NumberValue(int number) {
        this.number = number;
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        return new NumberVar(this.number);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
