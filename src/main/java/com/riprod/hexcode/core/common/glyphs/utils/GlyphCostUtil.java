package com.riprod.hexcode.core.common.glyphs.utils;

import java.util.Map;

import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public final class GlyphCostUtil {

    private static final float SOURCE_SCALE = 25.0f;

    private GlyphCostUtil() {
    }

    public static float contributionEffectiveness(float basis) {
        if (basis <= 0f) return 1.0f;
        return (float) Math.pow(10.0, -basis / SOURCE_SCALE);
    }

    public static float slotScale(Glyph glyph, HexContext hexContext, String slotKey, SlotConfig slotAsset) {
        if (slotAsset == null || slotAsset.getImpact() == null)
            return 1.0f;
        HexVar value = glyph.readSlot(slotKey, hexContext);
        double numeric = HexVarUtil.numberOrDefault(value, 0.0);
        return Impact.scale(slotAsset.getImpact(), numeric);
    }

    public static float volatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        if (asset == null)
            return 0f;
        float cost = glyph.computeBaseCost(asset);
        for (Map.Entry<String, SlotConfig> entry : asset.getSlots().entrySet()) {
            cost *= slotScale(glyph, hexContext, entry.getKey(), entry.getValue());
        }
        return cost;
    }
}
