package com.riprod.hexcode.builtin.hexCore.glyphs.shatter.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ShatterStyle {

    private static final String GLYPH_ID = "Shatter";
    private static final float LINE_THICKNESS = 0.06f;
    private static final float LINE_DURATION = 0.25f;
    private static final float HIT_LINE_THICKNESS = 0.1f;
    private static final float HIT_LINE_DURATION = 0.3f;

    private ShatterStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void renderLaunch(Vector3d position, Vector3d direction, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), position, accessor);
        VfxUtil.spawnStyleParticleDirected(overrides, asset(), position, accessor, direction);
    }

    public static void renderShardHit(Vector3d hitPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), hitPos, accessor);

        Vector3f color = VfxUtil.resolvePrimaryColor(ctx, asset());
        World world = accessor.getExternalData().getWorld();
        Vector3d lineEnd = new Vector3d(hitPos).add(0, 0.5, 0);
        VfxUtil.line(accessor, world, hitPos, lineEnd, color, HIT_LINE_THICKNESS, HIT_LINE_DURATION, 0);
    }

    public static void renderMiss(Vector3d endPos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset(), endPos, accessor);
    }
}
