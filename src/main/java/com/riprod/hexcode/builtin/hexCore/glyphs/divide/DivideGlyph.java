package com.riprod.hexcode.builtin.hexCore.glyphs.divide;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
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

public class DivideGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Divide";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(DivideGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(DivideGlyphSlots.B, hexContext);
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
                double denom = nb.getValue();
                yield new NumberVar(denom != 0 ? na.getValue() / denom : na.getValue());
            }
            case PositionVar pa -> {
                PositionVar pb = (PositionVar) b;
                Vector3d va = pa.getValue();
                Vector3d vb = pb.getValue();
                yield new PositionVar(new Vector3d(
                        vb.x != 0 ? va.x / vb.x : va.x,
                        vb.y != 0 ? va.y / vb.y : va.y,
                        vb.z != 0 ? va.z / vb.z : va.z), false);
            }
            case RotationVar ra -> {
                RotationVar rb = (RotationVar) b;
                Rotation3f va = ra.getValue();
                Rotation3f vb = rb.getValue();
                yield new RotationVar(new Rotation3f(
                        vb.x != 0 ? va.x / vb.x : va.x,
                        vb.y != 0 ? va.y / vb.y : va.y,
                        vb.z != 0 ? va.z / vb.z : va.z));
            }
            case ColorVar ca -> {
                ColorVar cb = (ColorVar) b;
                yield new ColorVar(
                        cb.getR() != 0 ? ca.getR() / cb.getR() : ca.getR(),
                        cb.getG() != 0 ? ca.getG() / cb.getG() : ca.getG(),
                        cb.getB() != 0 ? ca.getB() / cb.getB() : ca.getB(),
                        cb.getA() != 0 ? ca.getA() / cb.getA() : ca.getA());
            }
            default -> throw new TypeMismatchException(ID, a, b);
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
        HexVar self = hexContext.getVariable(glyph.getId());

        if (self != null) {
            return self;
        }
        return compute(glyph, hexContext);
    }
}
