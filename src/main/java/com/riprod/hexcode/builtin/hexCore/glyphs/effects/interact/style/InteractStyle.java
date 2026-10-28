package com.riprod.hexcode.builtin.hexCore.glyphs.effects.interact.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class InteractStyle {

    private InteractStyle() {
    }

    public static void renderImpact(GlyphAsset asset, ComponentAccessor<EntityStore> accessor, Vector3d position, HexContext ctx) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset, position, accessor);
    }
}
