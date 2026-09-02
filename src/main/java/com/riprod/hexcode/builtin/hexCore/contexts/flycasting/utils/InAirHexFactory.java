package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils;

import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class InAirHexFactory {
    private InAirHexFactory() {
    }

    public static Hex wrap(Glyph glyph) {
        if (glyph == null) {
            return null;
        }
        return new Hex(glyph);
    }
}
