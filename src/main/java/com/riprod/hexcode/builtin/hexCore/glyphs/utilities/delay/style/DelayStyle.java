package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay.style;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class DelayStyle {

    private static final String GLYPH_ID = "Delay";

    private DelayStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void renderAt(Vector3d pos, HexContext hexContext) {
        if (pos == null) return;
        VfxUtil.spawnPrimary(hexContext.getStyle(), asset(), pos, hexContext.getAccessor());
    }

    public static ColorLight resolveLight(HexContext ctx) {
        return ctx.getStyle().getPrimaryLight();
    }

    public static void renderExpiry(Vector3d pos, @Nullable HexContext ctx,
            ComponentAccessor<EntityStore> buffer) {
        HexStyleAsset overrides = ctx != null ? ctx.getStyle() : null;
        VfxUtil.spawnSecondary(overrides, asset(), pos, buffer);
    }
}
