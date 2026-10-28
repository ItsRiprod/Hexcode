package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ProjectileStyle {

    private ProjectileStyle() {
    }

    public static void renderLaunch(GlyphAsset asset, Vector3d position, Vector3d direction, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset, position, accessor);
        VfxUtil.spawnStyleParticleDirected(overrides, asset, position, accessor, direction);
    }

    public static void renderEntityHit(GlyphAsset asset, Vector3d projectilePos, Vector3d hitPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset, hitPos, accessor);
    }

    public static void renderBlockHit(GlyphAsset asset, Vector3d hitPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset, hitPos, accessor);
    }

    public static void renderMiss(Vector3d endPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        // I'm not sure what to do here tbh
    }
}
