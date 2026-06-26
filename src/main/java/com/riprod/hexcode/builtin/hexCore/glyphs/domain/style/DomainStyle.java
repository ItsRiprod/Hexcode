package com.riprod.hexcode.builtin.hexCore.glyphs.domain.style;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.domain.DomainGlyph;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class DomainStyle {

    private static final Vector3f DEFAULT_COLOR = new Vector3f(0.5f, 0.2f, 0.8f);
    private static final String DESPAWN_PARTICLE = "Conjure_Despawn";
    private static final String DESPAWN_SOUND = "SFX_Fireball_Miss";
    private static final String CONTESTED_PARTICLE = "Halt_Crystallize";
    private static final String CONTESTED_SOUND = "SFX_Ice_Break";

    private DomainStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(DomainGlyph.ID);
    }

    public static Vector3f resolveColor(HexColors colors) {
        if (colors == null || colors.getPrimaryColor() == null)
            return DEFAULT_COLOR;
        return HexColors.toVector3f(colors.getPrimaryColor());
    }

    public static void renderSpawn(Vector3d pos, float radius, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), pos, accessor);
    }

    public static void renderDespawn(Vector3d pos, float radius, HexColors colors,
            CommandBuffer<EntityStore> accessor) {
        VfxUtil.effect(DESPAWN_PARTICLE, DESPAWN_SOUND, pos, accessor);
    }

    public static void renderTrigger(Vector3d entityPos, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), entityPos, accessor);
    }

    public static void renderContested(Vector3d pos, HexColors colors,
            CommandBuffer<EntityStore> accessor) {
        VfxUtil.effect(CONTESTED_PARTICLE, CONTESTED_SOUND, pos, accessor);
    }

    public static void renderAmbient(Vector3d center, HexContext ctx,
            CommandBuffer<EntityStore> accessor) {

        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset(), center, accessor);
    }
}
