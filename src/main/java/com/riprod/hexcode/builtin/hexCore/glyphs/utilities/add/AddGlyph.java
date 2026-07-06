package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.add;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
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

public class AddGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Add";

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(AddGlyphSlots.A, hexContext);
        HexVar b = glyph.readSlot(AddGlyphSlots.B, hexContext);
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
                yield new NumberVar(na.getValue() + nb.getValue());
            }
            case PositionVar pa -> {
                PositionVar pb = (PositionVar) b;
                yield new PositionVar(
                        new Vector3d(pa.getValue()).add(pb.getValue()),
                        pa.isAbsolute() || pb.isAbsolute());
            }
            case RotationVar ra -> {
                RotationVar rb = (RotationVar) b;
                yield new RotationVar(new Rotation3f(ra.getValue()).add(rb.getValue()));
            }
            case ColorVar ca -> {
                ColorVar cb = (ColorVar) b;
                yield new ColorVar(
                        ca.getR() + cb.getR(),
                        ca.getG() + cb.getG(),
                        ca.getB() + cb.getB(),
                        Math.min(1.0, ca.getA() + cb.getA()));
            }
            default -> throw new TypeMismatchException(ID, a, b);
        };
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar self = hexContext.getVariable(glyph.getId());

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
