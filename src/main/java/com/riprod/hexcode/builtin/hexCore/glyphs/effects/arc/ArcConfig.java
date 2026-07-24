package com.riprod.hexcode.builtin.hexCore.glyphs.effects.arc;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ArcConfig extends GlyphConfig {

    public static final ArcConfig DEFAULTS = new ArcConfig();

    private String modelId = "Conjured_Anchor";
    private double referenceInterval = 0.75;

    public String getModel() {
        return modelId;
    }

    public double getReferenceInterval() {
        return referenceInterval;
    }

    public static final BuilderCodec<ArcConfig> CODEC = BuilderCodec
            .builder(ArcConfig.class, ArcConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Model", Codec.STRING, true),
                    (c, v) -> c.modelId = v, c -> c.modelId)
            .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("ReferenceInterval", Codec.DOUBLE, true),
                    (c, v) -> c.referenceInterval = v, c -> c.referenceInterval)
            .add()
            .build();
}
