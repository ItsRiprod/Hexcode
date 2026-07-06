package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ProjectileStyle {

    private static final String GLYPH_ID = "Projectile";

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
        VfxUtil.spawnStyleParticleDirected(overrides, projectile, position, accessor, direction);
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
        // I'm not sure what to do here tbh
    }
}
