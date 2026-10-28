package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.style;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.DomainGlyph;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class DomainStyle {

    private DomainStyle() {
    }

    public static void renderSpawn(DomainGlyph asset, Vector3d pos, float radius, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset, pos, accessor);
    }

    public static void renderDespawn(DomainGlyph asset, Vector3d pos, float radius, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        if (asset == null) return;
        VfxUtil.spawnFromConfig(overrides, asset, asset.getDespawnParticle(), asset.getDespawnSound(),
                pos, accessor);
    }

    public static void renderTrigger(DomainGlyph asset, Vector3d entityPos, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset, entityPos, accessor);
    }

    public static void renderContested(DomainGlyph asset, Vector3d pos, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        if (asset == null) return;
        VfxUtil.spawnFromConfig(overrides, asset, asset.getContestedParticle(), asset.getContestedSound(),
                pos, accessor);
    }

    public static void renderAmbient(DomainGlyph asset, Vector3d center, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {

        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset, center, accessor);
    }
}
