package com.riprod.hexcode.builtin.hexCore.glyphs.drain.style;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.VfxUtil;

public class DrainStyle {

    private static final String GLYPH_ID = "Drain";

    private DrainStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void renderTick(Vector3d pos, HexColors colors,
            ComponentAccessor<EntityStore> accessor) {
        VfxUtil.spawnPrimary(overridesOf(colors), asset(), pos, accessor);
    }

    public static void renderComplete(Vector3d pos, HexColors colors,
            ComponentAccessor<EntityStore> accessor) {
        VfxUtil.spawnSecondary(overridesOf(colors), asset(), pos, accessor);
    }

    private static HexStyleAsset overridesOf(HexColors colors) {
        if (colors == null) return null;
        HexStyleAsset s = HexStyleAsset.empty();
        if (colors.getPrimaryColor() != null) s.setPrimaryColor(colors.getPrimaryColor().clone());
        if (colors.getSecondaryColor() != null) s.setSecondaryColor(colors.getSecondaryColor().clone());
        s.setAlpha(colors.getPrimaryAlpha());
        return s;
    }
}
