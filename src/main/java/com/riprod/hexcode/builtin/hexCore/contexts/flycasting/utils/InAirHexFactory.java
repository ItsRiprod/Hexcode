package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class InAirHexFactory {
    private InAirHexFactory() {
    }

    public static Hex wrap(GlyphAsset asset, float volatility, float efficiency) {
        if (asset == null) {
            return null;
        }
        Glyph glyph = new Glyph(asset, volatility, efficiency);
        return new Hex(glyph);
    }
}
