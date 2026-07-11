package com.riprod.hexcode.builtin.hexCore.glyphs.elements.electrocute;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class ElectrocuteConfig extends ElementGlyphConfig {

    public static final ElectrocuteConfig DEFAULTS = new ElectrocuteConfig();

    private String statusEffect = "Lightning";
    private float durationPerComplexity = 1.0f;

    public String getStatusEffect() {
        return statusEffect;
    }

    public float getDurationPerComplexity() {
        return durationPerComplexity;
    }

    public static final BuilderCodec<ElectrocuteConfig> CODEC = BuilderCodec
            .builder(ElectrocuteConfig.class, ElectrocuteConfig::new, ElementGlyphConfig.BASE_CODEC)
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
