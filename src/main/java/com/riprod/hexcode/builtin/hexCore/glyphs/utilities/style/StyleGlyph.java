package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.style;

import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.hexes.component.HexColors;

public class StyleGlyph extends GlyphAsset {
    public static final String TYPE = "Style";

    private double[] resolveRgba(Glyph glyph, HexContext hexContext) {
        var buf = hexContext.getAccessor();
        var r = glyph.readSlot(StyleGlyphSlots.R, hexContext, NumberVar::new).position(buf).x;
        var g = glyph.readSlot(StyleGlyphSlots.G, hexContext, NumberVar::new).position(buf).y;
        var b = glyph.readSlot(StyleGlyphSlots.B, hexContext, NumberVar::new).position(buf).z;
        var a = glyph.readSlot(StyleGlyphSlots.A, hexContext, NumberVar::new).scalar();

        return new double[] {
                Math.clamp(r, 0, 255),
                Math.clamp(g, 0, 255),
                Math.clamp(b, 0, 255),
                Math.clamp(a, 0, 255)
        };
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        double[] rgba = resolveRgba(glyph, hexContext);
        return new ColorVar(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        var linked = GlyphAsset.of(glyph.linkedGlyph(StyleGlyphSlots.STYLE, hexContext), GlyphAsset.class);
        if (linked != null && linked.getEffects() != null) {
            var look = linked.getEffects().palette();
            look.setModel(linked.getEffects().getModel());
            hexContext.mutableEffects().compose(look);
        }

        if (hasAnyColorInput(glyph)) {
            double[] rgba = resolveRgba(glyph, hexContext);
            hexContext.setColors(toHexColorsOverride(rgba));
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
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
}
