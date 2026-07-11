package com.riprod.hexcode.builtin.hexCore.glyphs.elements.rebreathing;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class RebreathingConfig extends ElementGlyphConfig {

    public static final RebreathingConfig DEFAULTS = new RebreathingConfig();

    private String statusEffect = "Water";
    private float durationPerComplexity = 1.0f;

    public String getStatusEffect() {
        return statusEffect;
    }

    public float getDurationPerComplexity() {
        return durationPerComplexity;
    }

    public static final BuilderCodec<RebreathingConfig> CODEC = BuilderCodec
            .builder(RebreathingConfig.class, RebreathingConfig::new, ElementGlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("StatusEffect", Codec.STRING, true),
                    (c, v) -> c.statusEffect = v, c -> c.statusEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("DurationPerComplexity", Codec.FLOAT, true),
                    (c, v) -> c.durationPerComplexity = v, c -> c.durationPerComplexity)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .build();
}
