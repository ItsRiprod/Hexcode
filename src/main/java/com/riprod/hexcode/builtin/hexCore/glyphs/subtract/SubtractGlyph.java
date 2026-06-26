package com.riprod.hexcode.builtin.hexCore.glyphs.subtract;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.glyphs.variables.TypeMismatchException;

public class SubtractGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Subtract";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(SubtractGlyphSlots.A, hexContext, null);
        HexVar b = glyph.readSlot(SubtractGlyphSlots.B, hexContext, null);
        if (a == null && b == null) return null;
        if (b == null) return a;
        if (a == null) return negate(b);

        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();
        a = a.resolveSelf(b, buf);
        b = b.resolveSelf(a, buf);
        b = b.convertTo(a.getClass(), buf);

        return switch (a) {
            case NumberVar na -> {
                NumberVar nb = (NumberVar) b;
                yield new NumberVar(na.getValue() - nb.getValue());
            }
            case PositionVar pa -> {
                PositionVar pb = (PositionVar) b;
                yield new PositionVar(
                        new Vector3d(pa.getValue()).sub(pb.getValue()),
                        pa.isAbsolute() && !pb.isAbsolute());
            }
            case RotationVar ra -> {
                RotationVar rb = (RotationVar) b;
                yield new RotationVar(new Rotation3f(ra.getValue()).sub(rb.getValue()));
            }
            case ColorVar ca -> {
                ColorVar cb = (ColorVar) b;
                yield new ColorVar(
                        ca.getR() - cb.getR(),
                        ca.getG() - cb.getG(),
                        ca.getB() - cb.getB(),
                        ca.getA());
            }
            default -> throw new TypeMismatchException(ID, a, b);
        };
    }

    private HexVar negate(HexVar v) {
        return switch (v) {
            case NumberVar nv -> new NumberVar(nv.getValue() == null ? 0.0 : -nv.getValue());
            case PositionVar pv -> {
                Vector3d p = pv.getValue();
                yield p == null ? pv : new PositionVar(new Vector3d(-p.x, -p.y, -p.z), false);
            }
            case RotationVar rv -> {
                Rotation3f r = rv.getValue();
                yield r == null ? rv : new RotationVar(new Rotation3f(-r.x, -r.y, -r.z));
            }
            case ColorVar cv -> new ColorVar(-cv.getR(), -cv.getG(), -cv.getB(), cv.getA());
            default -> v;
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
        HexVar self = hexContext.getVariable(glyph.getId());

        if (self != null) {
            return self;
        }
        return compute(glyph, hexContext);
    }
}
