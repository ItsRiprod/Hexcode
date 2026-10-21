package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.multiply;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.glyphs.variables.TransformVar;

public class MultiplyGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Multiply";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(MultiplyGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(MultiplyGlyphSlots.B, hexContext);
        if (a == null || b == null || a.scalar() == 0 || b.scalar() == 0)
            return new NumberVar(0); // 0 * x = 0 // x * 0 = 0

        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();

        var baseType = a.type();

        return switch (baseType) {
            case POSITION -> {
                var vect = a.position(buf);
                var vb = b.position(buf);
                yield new PositionVar(vect.mul(vb));
            }
            case ROTATION -> {
                var v = a.rotation(buf);
                var vb = b.rotation(buf);
                yield new RotationVar(v.mul(vb));
            }
            case NUMBER -> new NumberVar(a.scalar() * b.scalar());
            case TRANSFORM -> {
                var t = a.transform(buf);
                var tb = b.transform(buf);
                yield new TransformVar(t.getPosition().mul(tb.getPosition()), t.getRotation().mul(tb.getRotation()));
            }
        };
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar result = compute(glyph, hexContext);

        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getOwnVariable(glyph.getId());

        if (self != null) {
            return self;
        }
        return compute(glyph, hexContext);
    }
}
