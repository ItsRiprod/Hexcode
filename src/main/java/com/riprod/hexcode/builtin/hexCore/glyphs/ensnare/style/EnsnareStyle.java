package com.riprod.hexcode.builtin.hexCore.glyphs.ensnare.style;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;
import java.util.List;

public class EnsnareStyle {

    private static final String GLYPH_ID = "Ensnare";

    private EnsnareStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void renderSeismicBurst(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), center, accessor);
    }

    public static void renderSpikeDamage(Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), pos, accessor);
    }

    public static void renderSpikeDespawn(Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        renderSpikeDespawn(pos, ctx, accessor, null);
    }

    public static void renderSpikeDespawn(Vector3d pos, HexContext ctx,
            ComponentAccessor<EntityStore> accessor, List<Ref<EntityStore>> recipients) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset(), pos, accessor, recipients);
    }
}
