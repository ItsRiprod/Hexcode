package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.round;

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

public class RoundGlyph implements GlyphHandler {
    public static final String ID = "Round";

    @Override
    public String getId() {
        return ID;
    }

    private static double op(double v) {
        return (double) Math.round(v);
    }

    private HexVar compute(Glyph glyph, HexContext hexContext) {
        HexVar a = glyph.readSlot(RoundGlyphSlots.A, hexContext);
        if (a == null)
            return null;
        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();

        var baseType = a.type();

        return switch (baseType) {
            case POSITION -> {
                var vect = a.position(buf);
                yield new PositionVar(new Vector3d(op(vect.x), op(vect.y), op(vect.z)));
            }
            case ROTATION -> {
                var v = a.rotation(buf);
                yield new RotationVar(new Rotation3f((float) op(v.x), (float) op(v.y), (float) op(v.z)));
            }
            case NUMBER -> new NumberVar(op(a.scalar()));
            case TRANSFORM -> {
                var t = a.transform(buf);
                var v = t.getPosition();
                yield new TransformVar(new Vector3d(op(v.x), op(v.y), op(v.z)), t.getRotation());
            }
        };
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
