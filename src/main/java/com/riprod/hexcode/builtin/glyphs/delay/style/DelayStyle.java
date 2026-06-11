package com.riprod.hexcode.builtin.glyphs.delay.style;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class DelayStyle {

    private static final String GLYPH_ID = "Delay";

    private DelayStyle() {
    }

    private static GlyphAsset asset() {
        return GlyphAsset.getAssetMap().getAsset(GLYPH_ID);
    }

    public static void render(HexContext hexContext) {
        Vector3d casterPos = HexVarUtil.position(
                hexContext.getVariable(hexContext.getDefaultSlot()), hexContext.getAccessor());
        if (casterPos == null) return;

        HexStyleAsset overrides = hexContext.getStyle();
        VfxUtil.spawnPrimary(overrides, asset(), casterPos, hexContext.getAccessor());
    }

    public static ColorLight resolveLight(HexContext ctx) {
        return ctx.getStyle().getPrimaryLight();
    }

    public static void renderExpiry(Vector3d pos, @Nullable HexColors colors,
            ComponentAccessor<EntityStore> buffer) {
        VfxUtil.spawnSecondary(overridesOf(colors), asset(), pos, buffer);
    }

    private static @Nullable HexStyleAsset overridesOf(@Nullable HexColors colors) {
        if (colors == null) return null;
        HexStyleAsset s = HexStyleAsset.empty();
        if (colors.getPrimaryColor() != null) s.setPrimaryColor(colors.getPrimaryColor().clone());
        if (colors.getSecondaryColor() != null) s.setSecondaryColor(colors.getSecondaryColor().clone());
        s.setAlpha(colors.getPrimaryAlpha());
        return s;
    }
}
