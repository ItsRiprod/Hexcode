package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ConcentrationConfig extends GlyphConfig {

    public static final ConcentrationConfig DEFAULTS = new ConcentrationConfig();

    public static final float DEFAULT_SUSTAIN_PER_SECOND = 3.0f;

    @Nullable
    private Impact sustainImpact;
    private double secondaryIntervalSeconds = 1.0;
    private double manaPerSecond = 1.0;
    private double resourceDrainPerSecond = 6.0;

    @Nullable
    public Impact getSustainImpact() {
        return sustainImpact;
    }

    public double getSecondaryIntervalSeconds() {
        return secondaryIntervalSeconds;
    }

    public double getManaPerSecond() {
        return manaPerSecond;
    }

    public double getResourceDrainPerSecond() {
        return resourceDrainPerSecond;
    }

    public static final BuilderCodec<ConcentrationConfig> CODEC = BuilderCodec
            .builder(ConcentrationConfig.class, ConcentrationConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("SustainImpact", Impact.CODEC, true),
                    (c, v) -> c.sustainImpact = v, c -> c.sustainImpact)
            .add()
            .append(new KeyedCodec<>("SecondaryIntervalSeconds", Codec.DOUBLE, true),
                    (c, v) -> c.secondaryIntervalSeconds = v, c -> c.secondaryIntervalSeconds)
            .add()
            .append(new KeyedCodec<>("ManaPerSecond", Codec.DOUBLE, true),
                    (c, v) -> c.manaPerSecond = v, c -> c.manaPerSecond)
            .add()
            .append(new KeyedCodec<>("ResourceDrainPerSecond", Codec.DOUBLE, true),
                    (c, v) -> c.resourceDrainPerSecond = v, c -> c.resourceDrainPerSecond)
            .add()
            .build();
}
