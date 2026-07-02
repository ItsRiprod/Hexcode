package com.riprod.hexcode.core.common.glyphs.utils;

import javax.annotation.Nullable;

import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.core.common.drawing.system.GlyphCreationManager;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class GlyphResolver {

    private GlyphResolver() {
    }

    @Nullable
    public static GlyphAsset resolve(ShapeStructure structure) {
        if (structure == null || structure.getShapes().isEmpty()) {
            return null;
        }
        return GlyphCreationManager.MatchGlyph(structure.getShapes());
    }
}
