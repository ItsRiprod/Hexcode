package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.arc.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ArcStyle {

    private static final float BEAM_THICKNESS = 0.15f;
    private static final float BEAM_DURATION = 0.5f;
    private static final int PARTICLES_PER_ARC = 8;

    private ArcStyle() {
    }

    public static void renderCast(GlyphAsset asset, ComponentAccessor<EntityStore> accessor, Vector3d position,
            HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset, position, accessor);
    }

    public static void renderArc(GlyphAsset asset, ComponentAccessor<EntityStore> accessor,
            Vector3d sourcePos, Vector3d targetPos, HexContext ctx) {
        Vector3f color = VfxUtil.resolvePrimaryColor(ctx, asset);
        VfxUtil.line(accessor, sourcePos, targetPos, color, BEAM_THICKNESS, BEAM_DURATION, 0,
                VfxUtil.resolveAlpha(ctx, asset));
        if (VfxUtil.resolveAlpha(ctx, asset) > 0f) {
            Color tint = VfxUtil.resolvePrimaryColorRaw(ctx, asset);
            VfxUtil.particleAlongPath(particleSystemId(asset), sourcePos, targetPos, PARTICLES_PER_ARC, tint, null,
                    accessor);
        }
    }

    public static void renderHit(GlyphAsset asset, ComponentAccessor<EntityStore> accessor, Vector3d position,
            HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset, position, accessor);
    }

    private static String particleSystemId(GlyphAsset asset) {
        if (asset == null || asset.getStyle() == null || asset.getStyle().getPrimaryParticle() == null) return null;
        return asset.getStyle().getPrimaryParticle().getSystemId();
    }
}
