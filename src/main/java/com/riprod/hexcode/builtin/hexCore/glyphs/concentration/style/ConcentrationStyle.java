package com.riprod.hexcode.builtin.hexCore.glyphs.concentration.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.concentration.ConcentrationGlyph;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ConcentrationStyle {

    private ConcentrationStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(ConcentrationGlyph.ID);
    }

    public static String resolveModelId(HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        String id = overrides != null ? overrides.getPrimaryModel() : null;
        if (id != null) return id;
        HexStyleAsset glyphStyle = asset() != null ? asset().getStyle() : null;
        return glyphStyle != null ? glyphStyle.getPrimaryModel() : null;
    }

    public static void renderSpawn(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), center, accessor);
    }

    public static void renderTick(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), center, accessor);
    }

    public static void renderEnd(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset(), center, accessor);
    }
}
