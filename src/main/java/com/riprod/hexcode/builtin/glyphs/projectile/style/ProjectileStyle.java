package com.riprod.hexcode.builtin.glyphs.projectile.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ProjectileStyle {

    private static final String GLYPH_ID = "Projectile";
    private static final Vector3f DEFAULT_COLOR = new Vector3f(1.0f, 0.8f, 0.3f);

    private ProjectileStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void renderLaunch(Vector3d position, Vector3d direction, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        GlyphAsset projectile = asset();
        VfxUtil.spawnPrimary(overrides, projectile, position, accessor);
        VfxUtil.spawnStyleParticle(overrides, projectile, position, accessor);
    }

    public static void renderEntityHit(Vector3d projectilePos, Vector3d hitPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        GlyphAsset projectile = asset();
        VfxUtil.spawnSecondary(overrides, projectile, hitPos, accessor);
    }

    public static void renderBlockHit(Vector3d hitPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        GlyphAsset projectile = asset();
        VfxUtil.spawnSecondary(overrides, projectile, hitPos, accessor);
    }

    public static void renderMiss(Vector3d endPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnStyleParticle(overrides, asset(), endPos, accessor);
    }

    private static Vector3f resolveColor(HexStyleAsset overrides) {
        Color c = overrides != null ? overrides.getPrimaryColor() : null;
        if (c == null) {
            HexStyleAsset glyphStyle = asset() != null ? asset().getStyle() : null;
            c = glyphStyle != null ? glyphStyle.getPrimaryColor() : null;
        }
        return c != null ? HexColors.toVector3f(c) : DEFAULT_COLOR;
    }
}
