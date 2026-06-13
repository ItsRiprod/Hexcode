package com.riprod.hexcode.core.common.glyphs.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.VolatilityTracker;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.registry.VolatilityAsset;
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

    default boolean consumeVolatility(Glyph glyph, HexContext hexContext) {
        VolatilityTracker tracker = hexContext.getVolatilityTracker();
        if (tracker == null)
            return true;
        float cost = VolatilityTracker.computeGlyphCost(glyph);
        if (cost <= 0)
            return true;
        boolean consumed = tracker.consumeVolatility(cost);
        return consumed;
    }


    @Nullable
    default <T extends GlyphConfig> T getConfig(@Nonnull Class<T> type) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(getId());
        if (asset == null)
            return null;
        // todo: finish config system
        return null;
    }

    default float computeAreaScale(double volume, GlyphAsset asset) {
        if (asset == null)
            return 1.0f;
        VolatilityAsset.AreaTax tax = asset.getVolatility().getAreaTax();
        if (tax == null || tax.getDefaultMagnitude() <= 0.0f)
            return 1.0f;
        double ratio = volume / tax.getDefaultMagnitude();
        if (ratio <= 1.0)
            return 1.0f;
        return (float) Math.pow(ratio, tax.getExponent());
    }

    static double sphereVolume(double radius) {
        return (4.0 / 3.0) * Math.PI * radius * radius * radius;
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
