package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.power;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.glyphs.variables.TypeMismatchException;

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
            return null;
        if (a == null)
            return b;
        if (b == null)
            return a;

        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();
        a = a.resolveSelf(b, buf);
        b = b.resolveSelf(a, buf);
        b = b.convertTo(a.getClass(), buf);

        return switch (a) {
            case NumberVar na -> {
                NumberVar nb = (NumberVar) b;
                yield new NumberVar(safePow(na.getValue(), nb.getValue()));
            }
            case PositionVar pa -> {
                PositionVar pb = (PositionVar) b;
                Vector3d va = pa.getValue();
                Vector3d vb = pb.getValue();
                yield new PositionVar(new Vector3d(
                        safePow(va.x, vb.x), safePow(va.y, vb.y), safePow(va.z, vb.z)),
                        pa.isAbsolute() && pb.isAbsolute());
            }
            case RotationVar ra -> {
                RotationVar rb = (RotationVar) b;
                Rotation3f va = ra.getValue();
                Rotation3f vb = rb.getValue();
                yield new RotationVar(new Rotation3f(
                        (float) safePow(va.x, vb.x),
                        (float) safePow(va.y, vb.y),
                        (float) safePow(va.z, vb.z)));
            }
            case ColorVar ca -> {
                ColorVar cb = (ColorVar) b;
                yield new ColorVar(
                        safePow(ca.getR(), cb.getR()),
                        safePow(ca.getG(), cb.getG()),
                        safePow(ca.getB(), cb.getB()),
                        safePow(ca.getA(), cb.getA()));
            }
            default -> throw new TypeMismatchException(ID, a, b);
        };
    }

    private static double safePow(double base, double exp) {
        double r = Math.pow(base, exp);
        return Double.isNaN(r) || Double.isInfinite(r) ? base : r;
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
