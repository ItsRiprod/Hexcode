package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.root;

import org.joml.Vector3d;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.api.execution.IllegalOperationException;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.power.PowerGlyphSlots;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.glyphs.variables.TransformVar;

public class RootGlyph extends GlyphAsset {
    public static final String TYPE = "Root";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(PowerGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(PowerGlyphSlots.B, hexContext);
        if (a == null || b == null)
            return new NumberVar(0);

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
        if (B == 0)
            throw new IllegalOperationException("zeroth root");
        if (A < 0 && B == Math.rint(B) && Math.abs(B % 2) == 1)
            return -Math.pow(-A, 1.0 / B);
        return Math.pow(A, 1.0 / B);
    }

    private float op(float A, float B) {
        return op((double) A, (double) B).floatValue();
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
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
