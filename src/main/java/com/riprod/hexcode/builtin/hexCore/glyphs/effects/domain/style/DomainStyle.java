package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.style;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.DomainConfig;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.DomainGlyph;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class DomainStyle {

    private DomainStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(DomainGlyph.ID);
    }

    private static @Nullable DomainConfig config(@Nullable GlyphAsset asset) {
        return asset != null && asset.getConfig() instanceof DomainConfig dc ? dc : null;
    }

    public static void renderSpawn(Vector3d pos, float radius, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), pos, accessor);
    }

    public static void renderDespawn(Vector3d pos, float radius, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        GlyphAsset asset = asset();
        DomainConfig config = config(asset);
        if (config == null) return;
        VfxUtil.spawnFromConfig(overrides, asset, config.getDespawnParticle(), config.getDespawnSound(),
                pos, accessor);
    }

    public static void renderTrigger(Vector3d entityPos, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), entityPos, accessor);
    }

    public static void renderContested(Vector3d pos, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        GlyphAsset asset = asset();
        DomainConfig config = config(asset);
        if (config == null) return;
        VfxUtil.spawnFromConfig(overrides, asset, config.getContestedParticle(), config.getContestedSound(),
                pos, accessor);
    }

    public static void renderAmbient(Vector3d center, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {

        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset(), center, accessor);
    }
}
