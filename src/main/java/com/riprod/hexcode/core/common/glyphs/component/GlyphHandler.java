package com.riprod.hexcode.core.common.glyphs.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphCostUtil;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public interface GlyphHandler {
    HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    void execute(Glyph glyph, HexContext hexContext);

    String getId();

    default HexVar readValue(Glyph glyph, HexContext hexContext) {
        return hexContext.getVariable(glyph.getId());
    }

    default float collectMana(Glyph glyph, GlyphAsset asset) {
        if (asset == null)
            return 0f;
        return asset.getManaConsumption()
                * ((1 - glyph.getEfficiency()) * 0.25f + 0.75f);
    }

    default void execute0(Glyph glyph, HexContext hexContext) {
        HexStats tracker = hexContext.getHexStats();
        if (tracker == null) {
            execute(glyph, hexContext);
            return;
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());

        float volatilityCost = getVolatilityCost(glyph, hexContext, asset);
        float currentVolatility = tracker.consumeVolatility(volatilityCost);
        if (currentVolatility <= 0f) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.VOLATILITY_DEPLETED);
            return;
        }

        float complexity = getComplexity(glyph, hexContext, asset);
        hexContext.addComplexity(complexity);

        execute(glyph, hexContext);
    }

    default float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return GlyphCostUtil.volatilityCost(glyph, hexContext, asset);
    }

    default float getComplexity(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        if (asset == null)
            return 0f;
        float base = asset.getVolatility().getInstantCost();
        Impact impact = asset.getConfig() != null ? asset.getConfig().getComplexityImpact() : null;
        return base * Impact.scale(impact, base);
    }

    default void addComplexity(HexContext hexContext, float amount) {
        if (amount == 0f)
            return;
        hexContext.addComplexity(amount);
    }


    @Nullable
    default <T extends GlyphConfig> T getConfig(@Nonnull Class<T> type) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(getId());
        if (asset == null)
            return null;
        GlyphConfig config = asset.getConfig();
        return type.isInstance(config) ? type.cast(config) : null;
    }

    default ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return null;
    }

    record ConfigBinding<T extends GlyphConfig>(Class<T> type, BuilderCodec<T> codec) {
        public static <T extends GlyphConfig> ConfigBinding<T> of(
                @Nonnull Class<T> type, @Nonnull BuilderCodec<T> codec) {
            return new ConfigBinding<>(type, codec);
        }
    }
}
