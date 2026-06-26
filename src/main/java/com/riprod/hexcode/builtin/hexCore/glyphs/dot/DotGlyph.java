package com.riprod.hexcode.builtin.hexCore.glyphs.dot;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;

public class DotGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final String ID = "Dot";

    @Override
    public String getId() {
        return ID;
    }

    @Nullable
    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar v = glyph.readSlot(DotGlyphSlots.V, hexContext);
        HexVar w = glyph.readSlot(DotGlyphSlots.W, hexContext);

        if (v == null && w == null) {
            return new NumberVar(0);
        }
        if (v == null || w == null) {
            return new NumberVar(v == null ? w.toScalar() : v.toScalar());
        }

        var posV = v.toPosition(hexContext.getAccessor());
        var posW = w.toPosition(hexContext.getAccessor());

        var vecV = posV.getValue();
        var vecW = posW.getValue();

        var dot = vecV.dot(vecW);

        return new NumberVar(dot);

    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        try {
            HexVar self = hexContext.getVariable(glyph.getId());

            if (self != null) {
                return self;
            }

            return compute(glyph, hexContext);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Error occurred while computing dot product");
            return null;
        }
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
