package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.power;

import org.joml.Vector3d;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
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

public class PowerGlyph implements GlyphHandler {
    public static final String ID = "Power";

    @Override
    public String getId() {
        return ID;
    }

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(PowerGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(PowerGlyphSlots.B, hexContext);
        if (a == null && b == null)
            return new NumberVar(1); // 0^0 = 1 because math ig
        if (b == null)
            return new NumberVar(1); // x^0 = 1
        if (a == null || b.scalar() == 0 || a.scalar() == 0) // early return if a is null (0^x) or b = 0 (x^0) since
                                                             // those are both constants
            return b.scalar() == 0 ? new NumberVar(1) /** 0^0=1 */
                    : new NumberVar(0); // 0^x = 0

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

    private Double op(Double A, Double B) {
        var r = Math.pow(A, B);
        return Double.isNaN(r) || Double.isInfinite(r) ? A : r;
    }

    private float op(float A, float B) {
        var r = (float) Math.pow(A, B);
        return Float.isNaN(r) || Float.isInfinite(r) ? A : r;
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
