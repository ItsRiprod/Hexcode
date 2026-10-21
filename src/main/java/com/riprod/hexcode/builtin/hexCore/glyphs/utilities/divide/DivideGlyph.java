package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.divide;

import org.joml.Vector3d;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.glyphs.variables.TransformVar;

public class DivideGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Divide";

    private Double op(Double A, Double B) {
        return B != 0 ? A / B : A;
    }

    private float op(float A, float B) {
        return B != 0 ? A / B : A;
    }

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(DivideGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(DivideGlyphSlots.B, hexContext);
        if (a == null || b == null || a.scalar() == 0 || b.scalar() == 0)
            return new NumberVar(0); // X/0 = 0 (assertion) and 0/X = 0

        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();

        var baseType = a.type();

        return switch (baseType) {
            case POSITION -> {
                var vect = a.position(buf);
                var vb = b.position(buf);
                yield new PositionVar(new Vector3d(op(vect.x, vb.x), op(vect.y, vb.y), op(vect.z, vb.z)));
            }
            case ROTATION -> {
                var v = a.rotation(buf);
                var vb = b.rotation(buf);
                yield new RotationVar(new Rotation3f(op(v.x, vb.x), op(v.y, vb.y), op(v.z, vb.z)));
            }
            case NUMBER -> new NumberVar(op(a.scalar(), b.scalar()));
            case TRANSFORM -> {
                var t = a.transform(buf);
                var v = t.getPosition();
                var r = t.getRotation();
                var tb = b.transform(buf);
                var vb = tb.getPosition();
                var rb = tb.getRotation();
                yield new TransformVar(new Vector3d(op(v.x, vb.x), op(v.y, vb.y), op(v.z, vb.z)),
                        new Rotation3f(op(r.x, rb.x), op(r.y, rb.y), op(r.z, rb.z)));
            }
        };
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar b = glyph.readSlot(DivideGlyphSlots.B, hexContext);
        if (hasZeroDivisor(b)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Attempted to divide by zero");
            return;
        }

        HexVar result = compute(glyph, hexContext);

        if (result != null) {
            glyph.writeOutput(result, hexContext);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private static boolean hasZeroDivisor(HexVar b) {
        if (b instanceof NumberVar nb) {
            return nb.getValue() == 0;
        }
        if (b instanceof PositionVar pb) {
            Vector3d v = pb.getValue();
            return v != null && (v.x == 0 || v.y == 0 || v.z == 0);
        }
        if (b instanceof RotationVar rb) {
            Rotation3f v = rb.getValue();
            return v != null && (v.x == 0 || v.y == 0 || v.z == 0);
        }
        if (b instanceof ColorVar cb) {
            return cb.getR() == 0 || cb.getG() == 0 || cb.getB() == 0 || cb.getA() == 0;
        }
        return false;
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
