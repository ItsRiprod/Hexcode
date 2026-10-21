package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.subtract;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.power.PowerGlyphSlots;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.glyphs.variables.TransformVar;

public class SubtractGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Subtract";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(PowerGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(PowerGlyphSlots.B, hexContext);
        if (a == null && b == null)
            return null;
        if (b == null)
            return a; // X - 0 = X, skip the math and return directly

        // set the type to a if a is not null, otherwise, set it to B so B negates
        // correctly
        var baseType = a == null ? b.type() : a.type();

        if (a == null)
            a = new NumberVar(0); // set A to 0 so that it results in 0 - B or -B

        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();

        return switch (baseType) {
            case POSITION -> {
                var vect = a.position(buf);
                var vb = b.position(buf);
                yield new PositionVar(vect.sub(vb)); // if A = 0, then (0,0,0) - (bx,by,bz) = -b
            }
            case ROTATION -> {
                var v = a.rotation(buf);
                var vb = b.rotation(buf);
                yield new RotationVar(v.sub(vb));
            }
            case NUMBER -> new NumberVar(a.scalar() - b.scalar());
            case TRANSFORM -> {
                var t = a.transform(buf);
                var v = t.getPosition();
                var r = t.getRotation();
                var tb = b.transform(buf);
                var vb = tb.getPosition();
                var rb = tb.getRotation();
                yield new TransformVar(v.sub(vb), r.sub(rb));
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
