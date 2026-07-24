package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.style;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public class StyleGlyph implements GlyphHandler {
    public static final String ID = "Style";

    @Override
    public String getId() {
        return ID;
    }

    private double[] resolveRgba(Glyph glyph, HexContext hexContext) {
        HexVar rIn = glyph.readSlot(StyleGlyphSlots.R, hexContext);
        HexVar gIn = glyph.readSlot(StyleGlyphSlots.G, hexContext);
        HexVar bIn = glyph.readSlot(StyleGlyphSlots.B, hexContext);
        HexVar aIn = glyph.readSlot(StyleGlyphSlots.A, hexContext);
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
        double a = aIn == null || aIn.toScalar() == null ? 255.0 : aIn.toScalar();

        return new double[] {
                clamp(r, 0, 255),
                clamp(g, 0, 255),
                clamp(b, 0, 255),
                clamp(a, 0, 255)
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
        HexStyleAsset linkedStyle = resolveLinkedGlyphStyle(glyph, hexContext);
        if (linkedStyle != null) hexContext.mutableStyle().applyOverride(linkedStyle);

        if (hasAnyColorInput(glyph)) {
            double[] rgba = resolveRgba(glyph, hexContext);
            hexContext.setColors(toHexColorsOverride(rgba));
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private static @Nullable HexStyleAsset resolveLinkedGlyphStyle(Glyph glyph, HexContext hexContext) {
        Slot styleSlot = glyph.getSlots().get(StyleGlyphSlots.STYLE);
        if (styleSlot == null) return null;
        String linkedId = styleSlot.getFirstLink();
        if (linkedId == null) return null;
        Glyph linked = hexContext.getGlyph(linkedId);
        if (linked == null) return null;
        GlyphAsset linkedAsset = GlyphAsset.getAssetMap().getAsset(linked.getGlyphId());
        return linkedAsset != null ? linkedAsset.getStyle() : null;
    }

    private static boolean hasAnyColorInput(Glyph glyph) {
        return glyph.getSlots().get(StyleGlyphSlots.R) != null
                && glyph.getSlots().get(StyleGlyphSlots.R).getFirstLink() != null
            || glyph.getSlots().get(StyleGlyphSlots.G) != null
                && glyph.getSlots().get(StyleGlyphSlots.G).getFirstLink() != null
            || glyph.getSlots().get(StyleGlyphSlots.B) != null
                && glyph.getSlots().get(StyleGlyphSlots.B).getFirstLink() != null;
    }

    private static HexColors toHexColorsOverride(double[] rgba) {
        HexColors c = new HexColors();
        c.setOverride(rgba[0] / 255.0, rgba[1] / 255.0, rgba[2] / 255.0, rgba[3] / 255.0);
        return c;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : Math.min(v, hi);
    }
}
