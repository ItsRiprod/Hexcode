package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward.WardGlyph;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.core.common.utilities.OrientedDebugUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class WardStyle {

    private static final double WARD_LINE_THICKNESS = 0.03;
    private static final float WARD_LINE_DURATION = 0.01f;

    private WardStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(WardGlyph.ID);
    }

    public static void renderSpawn(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset(), center, accessor);
    }

    public static void renderEnd(Vector3d center, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnTertiary(overrides, asset(), center, accessor);
    }

    public static void renderWardLine(Vector3d from, Vector3d to, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        Vector3f color = VfxUtil.resolvePrimaryColor(ctx, asset());
        OrientedDebugUtil.addCylinder(accessor, from, to, color, WARD_LINE_THICKNESS, WARD_LINE_DURATION, 0,
                VfxUtil.resolveAlpha(ctx, asset()));
    }
}
