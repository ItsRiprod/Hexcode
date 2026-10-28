package com.riprod.hexcode.builtin.hexCore.glyphs.effects.force;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class ForceGlyphStyle {

    private ForceGlyphStyle() {
    }

    public static void render(GlyphAsset asset, Vector3d targetPos, Vector3d forceDir, HexContext ctx,
            ComponentAccessor<EntityStore> accessor) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnPrimary(overrides, asset, targetPos, accessor);
        VfxUtil.spawnSecondary(overrides, asset, targetPos, accessor);
    }
}
