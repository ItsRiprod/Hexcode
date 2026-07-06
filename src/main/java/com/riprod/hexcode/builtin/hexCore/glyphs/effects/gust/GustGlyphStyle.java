package com.riprod.hexcode.builtin.hexCore.glyphs.effects.gust;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class GustGlyphStyle {

    private static final String GLYPH_ID = "Gust";

    private GustGlyphStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void render(Vector3d center, double radius, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        if (radius <= 3.0) {
            VfxUtil.spawnPrimary(overrides, asset(), center, accessor);
        } else if (radius <= 7.0) {
            VfxUtil.spawnSecondary(overrides, asset(), center, accessor);
        } else {
            VfxUtil.spawnTertiary(overrides, asset(), center, accessor);
        }
    }
}
