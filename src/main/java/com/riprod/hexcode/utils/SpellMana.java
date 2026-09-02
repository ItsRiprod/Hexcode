package com.riprod.hexcode.utils;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class SpellMana {

    private static final int MAX_COMPONENT_DEPTH = 256;

    private SpellMana() {
    }

    public static float computeTotalMana(Hex hex, @Nullable ComponentAccessor<EntityStore> accessor) {
        return computeTotalMana(hex, accessor, 0);
    }

    private static float computeTotalMana(Hex hex, @Nullable ComponentAccessor<EntityStore> accessor,
            int depth) {
        if (hex == null || depth > MAX_COMPONENT_DEPTH) return 0f;
        float total = 0f;
        for (Glyph glyph : hex.getGlyphs()) {
            if (glyph == null) continue;
            if (glyph.isComponentInstance()) {
                total += computeTotalMana(glyph.payloadView(accessor), accessor, depth + 1);
                continue;
            }
            GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
            if (asset == null) continue;
            GlyphHandler handler = GlyphRegistry.get(asset.getHandler());
            if (handler == null) continue;
            total += handler.collectMana(glyph, asset);
        }
        return total;
    }
}
