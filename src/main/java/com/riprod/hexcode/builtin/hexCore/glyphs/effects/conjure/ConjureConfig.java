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
    private String softCollisionId = "Hexcode_Conjure_SoftCollision";
    private String anchorModelId = "Conjured_Anchor";
    private double maxCorrectionPerTick = 0.35;
    private double correctionEpsilon = 0.02;
    private double defaultEntityHalfExtent = 0.5;

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

    public String getSoftCollisionId() {
        return softCollisionId;
    }

    public String getAnchorModelId() {
        return anchorModelId;
    }

    public double getMaxCorrectionPerTick() {
        return maxCorrectionPerTick;
    }

    public double getCorrectionEpsilon() {
        return correctionEpsilon;
    }

    public double getDefaultEntityHalfExtent() {
        return defaultEntityHalfExtent;
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
            .append(new KeyedCodec<>("SoftCollision", Codec.STRING, true),
                    (c, v) -> c.softCollisionId = v, c -> c.softCollisionId)
            .addValidatorLate(() -> HitboxCollisionConfig.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("AnchorModel", Codec.STRING, true),
                    (c, v) -> c.anchorModelId = v, c -> c.anchorModelId)
            .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("MaxCorrectionPerTick", Codec.DOUBLE, true),
                    (c, v) -> c.maxCorrectionPerTick = v, c -> c.maxCorrectionPerTick)
            .add()
            .append(new KeyedCodec<>("CorrectionEpsilon", Codec.DOUBLE, true),
                    (c, v) -> c.correctionEpsilon = v, c -> c.correctionEpsilon)
            .add()
            .append(new KeyedCodec<>("DefaultEntityHalfExtent", Codec.DOUBLE, true),
                    (c, v) -> c.defaultEntityHalfExtent = v, c -> c.defaultEntityHalfExtent)
            .add()
            .build();
}
