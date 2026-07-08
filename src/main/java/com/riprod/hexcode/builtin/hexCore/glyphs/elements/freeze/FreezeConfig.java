package com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class FreezeConfig extends ElementGlyphConfig {

    public static final FreezeConfig DEFAULTS = new FreezeConfig();

    private String statusEffect = "Ice";
    private String iceBlockId = "Rock_Ice";
    private float durationPerComplexity = 1.0f;
    private float minDuration = 1.0f;
    private float maxDuration = 30.0f;

    public String getStatusEffect() {
        return statusEffect;
    }

    public String getIceBlockId() {
        return iceBlockId;
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

    public static final BuilderCodec<FreezeConfig> CODEC = BuilderCodec
            .builder(FreezeConfig.class, FreezeConfig::new, ElementGlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("StatusEffect", Codec.STRING, true),
                    (c, v) -> c.statusEffect = v, c -> c.statusEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("IceBlock", Codec.STRING, true),
                    (c, v) -> c.iceBlockId = v, c -> c.iceBlockId)
            .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
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
