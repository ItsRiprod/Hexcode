package com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ConjureConfig extends GlyphConfig {

    public static final ConjureConfig DEFAULTS = new ConjureConfig();

    private double boxHalfExtent = 0.5;
    private double minAxisSize = 1.0;
    private float spatialQueryInterval = 0.2f;
    private String hardCollisionId = "Hexcode_Conjure_HardCollision";
    private String anchorModelId = "Conjured_Anchor";

    public double getBoxHalfExtent() {
        return boxHalfExtent;
    }

    public double getMinAxisSize() {
        return minAxisSize;
    }

    public float getSpatialQueryInterval() {
        return spatialQueryInterval;
    }

    public String getHardCollisionId() {
        return hardCollisionId;
    }

    public String getAnchorModelId() {
        return anchorModelId;
    }

    public static final BuilderCodec<ConjureConfig> CODEC = BuilderCodec
            .builder(ConjureConfig.class, ConjureConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("BoxHalfExtent", Codec.DOUBLE, true),
                    (c, v) -> c.boxHalfExtent = v, c -> c.boxHalfExtent)
            .add()
            .append(new KeyedCodec<>("MinAxisSize", Codec.DOUBLE, true),
                    (c, v) -> c.minAxisSize = v, c -> c.minAxisSize)
            .add()
            .append(new KeyedCodec<>("SpatialQueryInterval", Codec.FLOAT, true),
                    (c, v) -> c.spatialQueryInterval = v, c -> c.spatialQueryInterval)
            .add()
            .append(new KeyedCodec<>("HardCollision", Codec.STRING, true),
                    (c, v) -> c.hardCollisionId = v, c -> c.hardCollisionId)
            .addValidatorLate(() -> HitboxCollisionConfig.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("AnchorModel", Codec.STRING, true),
                    (c, v) -> c.anchorModelId = v, c -> c.anchorModelId)
            .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
