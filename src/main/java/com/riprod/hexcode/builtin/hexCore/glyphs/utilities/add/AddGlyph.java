package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.add;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
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
import com.riprod.hexcode.core.common.glyphs.variables.TransformVar;

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
        
        var baseType = a.type();

        return switch (baseType) {
            case POSITION -> new PositionVar(a.position(buf).add(b.position(buf)));
            case ROTATION -> new RotationVar(a.rotation(buf).add(b.rotation(buf)));
            case NUMBER -> new NumberVar(a.scalar() + b.scalar());
            case TRANSFORM -> {
                var transformA = a.transform(buf);
                var transformB = b.transform(buf);
                yield new TransformVar(transformA.getPosition().add(transformB.getPosition()), transformA.getRotation().add(transformB.getRotation()));
            }
            default -> null;  // smth went wrong
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
