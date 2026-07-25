package com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class FortifyConfig extends ElementGlyphConfig {

    public static final FortifyConfig DEFAULTS = new FortifyConfig();

    private String effectId = "Hexcode_Fortify";
    private float durationPerResource = 1.0f;

    public String getEffectId() {
        return effectId;
    }

    public float getDurationPerResource() {
        return durationPerResource;
    }

    public static final BuilderCodec<FortifyConfig> CODEC = BuilderCodec
            .builder(FortifyConfig.class, FortifyConfig::new, ElementGlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("DurationPerResource", Codec.FLOAT, true),
                    (c, v) -> c.durationPerResource = v, c -> c.durationPerResource)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .build();
}
