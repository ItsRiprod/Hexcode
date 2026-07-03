package com.riprod.hexcode.builtin.hexCore.glyphs.concentration;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ConcentrationConfig extends GlyphConfig {

    public static final ConcentrationConfig DEFAULTS = new ConcentrationConfig();

    private double volatilityBonusFraction = 0.5;
    private double secondaryIntervalSeconds = 1.0;

    public double getVolatilityBonusFraction() {
        return volatilityBonusFraction;
    }

    public double getSecondaryIntervalSeconds() {
        return secondaryIntervalSeconds;
    }

    public static final BuilderCodec<ConcentrationConfig> CODEC = BuilderCodec
            .builder(ConcentrationConfig.class, ConcentrationConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("VolatilityBonusFraction", Codec.DOUBLE, true),
                    (c, v) -> c.volatilityBonusFraction = v, c -> c.volatilityBonusFraction)
            .add()
            .append(new KeyedCodec<>("SecondaryIntervalSeconds", Codec.DOUBLE, true),
                    (c, v) -> c.secondaryIntervalSeconds = v, c -> c.secondaryIntervalSeconds)
            .add()
            .build();
}
