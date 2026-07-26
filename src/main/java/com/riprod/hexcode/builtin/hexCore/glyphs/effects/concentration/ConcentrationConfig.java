package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ConcentrationConfig extends GlyphConfig {

    public static final ConcentrationConfig DEFAULTS = new ConcentrationConfig();

    public static final float DEFAULT_SUSTAIN_PER_SECOND = 3.0f;

    @Nullable
    private Impact sustainImpact;
    @Nullable
    private Impact manaImpact;
    @Nullable
    private Impact staminaImpact;
    @Nullable
    private Impact healthImpact;
    private double secondaryIntervalSeconds = 1.0;
    private String healthDamageCauseId = "Arcane";

    public String getHealthDamageCauseId() {
        return healthDamageCauseId;
    }

    @Nullable
    public Impact getSustainImpact() {
        return sustainImpact;
    }

    @Nullable
    public Impact getManaImpact() {
        return manaImpact;
    }

    @Nullable
    public Impact getStaminaImpact() {
        return staminaImpact;
    }

    @Nullable
    public Impact getHealthImpact() {
        return healthImpact;
    }

    public double getSecondaryIntervalSeconds() {
        return secondaryIntervalSeconds;
    }

    public static final BuilderCodec<ConcentrationConfig> CODEC = BuilderCodec
            .builder(ConcentrationConfig.class, ConcentrationConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("SustainImpact", Impact.CODEC, true),
                    (c, v) -> c.sustainImpact = v, c -> c.sustainImpact)
            .add()
            .append(new KeyedCodec<>("ManaImpact", Impact.CODEC, true),
                    (c, v) -> c.manaImpact = v, c -> c.manaImpact)
            .add()
            .append(new KeyedCodec<>("StaminaImpact", Impact.CODEC, true),
                    (c, v) -> c.staminaImpact = v, c -> c.staminaImpact)
            .add()
            .append(new KeyedCodec<>("HealthImpact", Impact.CODEC, true),
                    (c, v) -> c.healthImpact = v, c -> c.healthImpact)
            .add()
            .append(new KeyedCodec<>("SecondaryIntervalSeconds", Codec.DOUBLE, true),
                    (c, v) -> c.secondaryIntervalSeconds = v, c -> c.secondaryIntervalSeconds)
            .add()
            .append(new KeyedCodec<>("HealthDamageCause", DamageCause.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.healthDamageCauseId = v, c -> c.healthDamageCauseId)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
