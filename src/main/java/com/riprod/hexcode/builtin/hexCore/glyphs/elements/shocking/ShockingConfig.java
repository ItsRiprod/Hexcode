package com.riprod.hexcode.builtin.hexCore.glyphs.elements.shocking;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class ShockingConfig extends ElementGlyphConfig {

    public static final ShockingConfig DEFAULTS = new ShockingConfig();

    private String statusEffect = "Lightning";
    private float durationPerComplexity = 1.0f;
    private float minDuration = 1.0f;
    private float maxDuration = 30.0f;

    public String getStatusEffect() {
        return statusEffect;
    }

    public float getDurationPerComplexity() {
        return durationPerComplexity;
    }

    public float getMinDuration() {
        return minDuration;
    }

    public float getMaxDuration() {
        return maxDuration;
    }

    public static final BuilderCodec<ShockingConfig> CODEC = BuilderCodec
            .builder(ShockingConfig.class, ShockingConfig::new, ElementGlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("StatusEffect", Codec.STRING, true),
                    (c, v) -> c.statusEffect = v, c -> c.statusEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("DurationPerComplexity", Codec.FLOAT, true),
                    (c, v) -> c.durationPerComplexity = v, c -> c.durationPerComplexity)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .append(new KeyedCodec<>("MinDuration", Codec.FLOAT, true),
                    (c, v) -> c.minDuration = v, c -> c.minDuration)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .append(new KeyedCodec<>("MaxDuration", Codec.FLOAT, true),
                    (c, v) -> c.maxDuration = v, c -> c.maxDuration)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .build();
}
