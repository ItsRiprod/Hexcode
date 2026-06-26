package com.riprod.hexcode.builtin.hexCore.glyphs.cross;

import javax.annotation.Nullable;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;

public class CrossGlyph implements GlyphHandler {
    public static final String ID = "Cross";

    @Override
    public String getId() {
        return ID;
    }

    @Nullable
    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar v = glyph.readSlot(CrossGlyphSlots.V, hexContext);
        HexVar w = glyph.readSlot(CrossGlyphSlots.W, hexContext);

        if (v == null && w == null) {
            return null;
        }
        if (v == null || w == null) {
            return v == null ? w : v;
        }

        var posV = v.toPosition(hexContext.getAccessor());
        var posW = w.toPosition(hexContext.getAccessor());

        var vecV = posV.getValue();
        var vecW = posW.getValue();

        var dot = vecV.cross(vecW);

        return new PositionVar(dot);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getVariable(glyph.getId());

        if (self != null) {
            return self;
        }
        return compute(glyph, hexContext);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar result = compute(glyph, hexContext);
        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }
        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
