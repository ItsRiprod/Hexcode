package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.root;

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

public class RootGlyph implements GlyphHandler {
    public static final String ID = "Root";

    @Override
    public String getId() {
        return ID;
    }

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(RootGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(RootGlyphSlots.B, hexContext);
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;

        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();
        a = a.resolveSelf(b, buf);
        b = b.resolveSelf(a, buf);
        b = b.convertTo(a.getClass(), buf);

        return switch (a) {
            case NumberVar na -> {
                NumberVar nb = (NumberVar) b;
                yield new NumberVar(safeRoot(na.getValue(), nb.getValue()));
            }
            case PositionVar pa -> {
                PositionVar pb = (PositionVar) b;
                Vector3d va = pa.getValue();
                Vector3d vb = pb.getValue();
                yield new PositionVar(new Vector3d(
                        safeRoot(va.x, vb.x), safeRoot(va.y, vb.y), safeRoot(va.z, vb.z)),
                        pa.isAbsolute() && pb.isAbsolute());
            }
            case RotationVar ra -> {
                RotationVar rb = (RotationVar) b;
                Rotation3f va = ra.getValue();
                Rotation3f vb = rb.getValue();
                yield new RotationVar(new Rotation3f(
                        (float) safeRoot(va.x, vb.x),
                        (float) safeRoot(va.y, vb.y),
                        (float) safeRoot(va.z, vb.z)));
            }
            case ColorVar ca -> {
                ColorVar cb = (ColorVar) b;
                yield new ColorVar(
                        safeRoot(ca.getR(), cb.getR()),
                        safeRoot(ca.getG(), cb.getG()),
                        safeRoot(ca.getB(), cb.getB()),
                        safeRoot(ca.getA(), cb.getA()));
            }
            default -> throw new TypeMismatchException(ID, a, b);
        };
    }

    private static double safeRoot(double radicand, double degree) {
        if (degree == 0) return radicand;
        double r = Math.pow(radicand, 1.0 / degree);
        return Double.isNaN(r) || Double.isInfinite(r) ? radicand : r;
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
