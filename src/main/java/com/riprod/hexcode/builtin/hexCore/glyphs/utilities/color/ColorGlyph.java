package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.color;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

import org.joml.Vector3d;

public class ColorGlyph implements GlyphHandler {
    public static final String ID = "Color";

    private static final double CHANNEL_MAX = 255.0;

    @Override
    public String getId() {
        return ID;
    }

    private double[] resolveRgba(Glyph glyph, HexContext hexContext) {
        HexVar rIn = glyph.readSlot(ColorGlyphSlots.R, hexContext);
        HexVar gIn = glyph.readSlot(ColorGlyphSlots.G, hexContext);
        HexVar bIn = glyph.readSlot(ColorGlyphSlots.B, hexContext);
        HexVar aIn = glyph.readSlot(ColorGlyphSlots.A, hexContext);
        ComponentAccessor<EntityStore> buf = hexContext.getAccessor();

        double r, g, b;
        Vector3d splat = vectorSplat(rIn, gIn, bIn, buf);
        if (splat != null) {
            r = splat.x;
            g = splat.y;
            b = splat.z;
        } else {
            r = rIn == null || rIn.toScalar() == null ? 0.0 : rIn.toScalar();
            g = gIn == null || gIn.toScalar() == null ? 0.0 : gIn.toScalar();
            b = bIn == null || bIn.toScalar() == null ? 0.0 : bIn.toScalar();
        }
        double a = aIn == null || aIn.toScalar() == null ? CHANNEL_MAX : aIn.toScalar();

        return new double[] {
                clamp(r, 0, CHANNEL_MAX),
                clamp(g, 0, CHANNEL_MAX),
                clamp(b, 0, CHANNEL_MAX),
                clamp(a, 0, CHANNEL_MAX)
        };
    }

    private static Vector3d vectorSplat(HexVar r, HexVar g, HexVar b, ComponentAccessor<EntityStore> buf) {
        for (HexVar v : new HexVar[] { r, g, b }) {
            if (v instanceof PositionVar || v instanceof RotationVar
                    || v instanceof EntityVar || v instanceof BlockVar) {
                PositionVar pv = v.toPosition(buf);
                if (pv != null && pv.getValue() != null) return pv.getValue();
            }
            if (v instanceof ColorVar cv) {
                return new Vector3d(cv.getR(), cv.getG(), cv.getB());
            }
        }
        return null;
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        double[] rgba = resolveRgba(glyph, hexContext);
        return new ColorVar(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        double[] rgba = resolveRgba(glyph, hexContext);

        if (isWired(glyph, ColorGlyphSlots.R) || isWired(glyph, ColorGlyphSlots.G)
                || isWired(glyph, ColorGlyphSlots.B)) {
            HexStyleAsset style = hexContext.mutableStyle();
            style.setPrimaryColor(new Color(toByte(rgba[0]), toByte(rgba[1]), toByte(rgba[2])));
        }

        // an unwired alpha slot must not clobber an upstream Alpha 0 and re-enable its particles
        if (isWired(glyph, ColorGlyphSlots.A)) {
            hexContext.mutableStyle().setAlpha((float) (rgba[3] / CHANNEL_MAX));
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private static boolean isWired(Glyph glyph, String slotKey) {
        Slot slot = glyph.getSlots().get(slotKey);
        return slot != null && slot.getFirstLink() != null;
    }

    private static byte toByte(double channel) {
        return (byte) Math.round(channel);
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : Math.min(v, hi);
    }
}
