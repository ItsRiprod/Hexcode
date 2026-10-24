package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.color;

import com.hypixel.hytale.protocol.Color;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.ColorVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;

public class ColorGlyph implements GlyphHandler {
    public static final String ID = "Color";

    private static final double CHANNEL_MAX = 255.0;

    @Override
    public String getId() {
        return ID;
    }

    private double[] resolveRgba(Glyph glyph, HexContext hexContext) {
        var buf = hexContext.getAccessor();
        var r = glyph.readSlot(ColorGlyphSlots.R, hexContext, NumberVar::new).position(buf).x;
        var g = glyph.readSlot(ColorGlyphSlots.G, hexContext, NumberVar::new).position(buf).x;
        var b = glyph.readSlot(ColorGlyphSlots.B, hexContext, NumberVar::new).position(buf).x;
        var a = glyph.readSlot(ColorGlyphSlots.A, hexContext, NumberVar::new).scalar();

        return new double[] {
                Math.clamp(r, 0, CHANNEL_MAX),
                Math.clamp(g, 0, CHANNEL_MAX),
                Math.clamp(b, 0, CHANNEL_MAX),
                Math.clamp(a, 0, CHANNEL_MAX)
        };
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        double[] rgba = resolveRgba(glyph, hexContext);
        return new ColorVar(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        double[] rgba = resolveRgba(glyph, hexContext);

        var style = hexContext.mutableStyle();
        if (isWired(glyph, ColorGlyphSlots.R) || isWired(glyph, ColorGlyphSlots.G)
                || isWired(glyph, ColorGlyphSlots.B)) {
            style.setPrimaryColor(new Color(toByte(rgba[0]), toByte(rgba[1]), toByte(rgba[2])));
        }

        // an unwired alpha slot must not clobber an upstream Alpha 0 and re-enable its
        // particles
        if (isWired(glyph, ColorGlyphSlots.A)) {
            style.setAlpha((float) (rgba[3] / CHANNEL_MAX));
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
}
