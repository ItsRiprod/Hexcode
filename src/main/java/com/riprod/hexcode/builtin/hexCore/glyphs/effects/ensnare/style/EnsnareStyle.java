package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;
import java.util.List;

public class EnsnareStyle {

    private EnsnareStyle() {
    }

    public static void renderSeismicBurst(GlyphAsset asset, Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset, center, accessor);
    }

    public static void renderSpikeDamage(GlyphAsset asset, Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset, pos, accessor);
    }

    public static void renderSpikeDespawn(GlyphAsset asset, Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        renderSpikeDespawn(asset, pos, ctx, accessor, null);
    }

    public static void renderSpikeDespawn(GlyphAsset asset, Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor, List<Ref<EntityStore>> recipients) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset, pos, accessor, recipients);
    }
}
