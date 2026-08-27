package com.riprod.hexcode.core.common.glyphs.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.component.ResourcePoolComponent;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
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
        return hexContext.getOwnVariable(glyph.getId());
    }

    default float collectMana(Glyph glyph, GlyphAsset asset) {
        if (asset == null)
            return 0f;
        return asset.getManaConsumption()
                * ((1 - glyph.getEfficiency()) * 0.25f + 0.75f);
    }

    default void execute0(Glyph glyph, HexContext hexContext) {
        HexCast cast = hexContext.cast();
        if (cast == null) {
            execute(glyph, hexContext);
            return;
        }
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());

        float volatilityCost = getVolatilityCost(glyph, hexContext, asset);
        float currentVolatility = cast.volatility().consume(volatilityCost);
        if (!hexContext.policy().isBypassVolatilityDepletion() && currentVolatility <= 0f) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.VOLATILITY_DEPLETED);
            return;
        }

        if (asset != null) {
            String source = glyph.getGlyphId();
            for (ShapeAsset shape : asset.getShapes()) {
                String resource = shape.getStatResource();
                if (resource == null) continue;
                float raw = Impact.scale(shape.getStatResourceImpact(), shape.getStatContribution());
                if (raw <= 0f) continue;
                ResourcePoolComponent pools = cast.mutableResources();
                float effectiveness = GlyphCostUtil.contributionEffectiveness(
                        pools.getBasis(resource, source));
                pools.addResource(resource, source, raw * effectiveness);
            }
        }

        execute(glyph, hexContext);
    }

    default float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return GlyphCostUtil.volatilityCost(glyph, hexContext, asset);
    }


    @Nullable
    default <T extends GlyphConfig> T getConfig(@Nonnull Class<T> type, @Nullable GlyphAsset asset) {
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
