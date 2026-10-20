package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.cross;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
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

        var vecV = v.position(hexContext.getAccessor());
        var vecW = w.position(hexContext.getAccessor());

        var cross = new Vector3d(vecV).cross(vecW);

        return new PositionVar(cross);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getOwnVariable(glyph.getId());

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
