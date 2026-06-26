package com.riprod.hexcode.builtin.hexCore.glyphs.conjure.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ConjureStyle {

    private static final String GLYPH_ID = "Conjure";
    private static final Vector3f DEFAULT_COLOR = new Vector3f(0.2f, 0.6f, 1.0f);

    private ConjureStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void renderSpawn(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), center, accessor);
    }

    public static void renderTrigger(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), center, accessor);
    }

    public static void renderDespawn(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset(), center, accessor);
    }

    public static Vector3f resolveColor(HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Color c = overrides != null ? overrides.getPrimaryColor() : null;
        if (c == null) {
            HexStyleAsset glyphStyle = asset() != null ? asset().getStyle() : null;
            c = glyphStyle != null ? glyphStyle.getPrimaryColor() : null;
        }
        return c != null ? HexColors.toVector3f(c) : DEFAULT_COLOR;
    }
}
