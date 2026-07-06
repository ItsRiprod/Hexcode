package com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ArcStyle {

    private static final String GLYPH_ID = "Arc";
    private static final float BEAM_THICKNESS = 0.15f;
    private static final float BEAM_DURATION = 0.5f;
    private static final int PARTICLES_PER_ARC = 8;

    private ArcStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void renderArc(ComponentAccessor<EntityStore> accessor, World world,
            Vector3d sourcePos, Vector3d targetPos, HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        Vector3f color = VfxUtil.resolvePrimaryColor(ctx, asset());
        VfxUtil.line(accessor, world, sourcePos, targetPos, color, BEAM_THICKNESS, BEAM_DURATION, 0);
        Color tint = VfxUtil.resolvePrimaryColorRaw(ctx, asset());
        VfxUtil.particleAlongPath(particleSystemId(), sourcePos, targetPos, PARTICLES_PER_ARC, tint, null, accessor);
        if (asset() != null && asset().getStyle() != null && asset().getStyle().getPrimarySound() != null) {
            VfxUtil.sound(asset().getStyle().getPrimarySound(), sourcePos, accessor);
        }
    }

    public static void renderHit(ComponentAccessor<EntityStore> accessor, Vector3d position,
            HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), position, accessor);
    }

    public static void renderFizzle(ComponentAccessor<EntityStore> accessor, Vector3d position,
            HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnStyleParticle(overrides, asset(), position, accessor);
    }

    private static String particleSystemId() {
        GlyphAsset a = asset();
        if (a == null || a.getStyle() == null || a.getStyle().getPrimaryParticle() == null) return null;
        return a.getStyle().getPrimaryParticle().getSystemId();
    }
}
