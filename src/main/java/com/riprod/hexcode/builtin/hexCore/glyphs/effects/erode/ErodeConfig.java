package com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ErodeConfig extends GlyphConfig {

    public static final ErodeConfig DEFAULTS = new ErodeConfig();

    private boolean impactEntities = true;
    private int minTier = 1;
    private int maxTier = 6;
    private float blockDamageScale = 0.05f;
    private int tierBucketWidth = 4;
    private String effectId = "Hexcode_Erode";
    private String toolAssetPrefix = "Hexcode_Erode_Tool_T";

    public boolean canImpactEntities() {
        return impactEntities;
    }

    public int getMinTier() {
        return minTier;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public float getBlockDamageScale() {
        return blockDamageScale;
    }

    public int getTierBucketWidth() {
        return tierBucketWidth;
    }

    public String getEffectId() {
        return effectId;
    }

    public String getToolAssetPrefix() {
        return toolAssetPrefix;
    }

    public static final BuilderCodec<ErodeConfig> CODEC = BuilderCodec
            .builder(ErodeConfig.class, ErodeConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ImpactEntities", Codec.BOOLEAN, true),
                    (c, v) -> c.impactEntities = v, c -> c.impactEntities)
            .add()
            .append(new KeyedCodec<>("MinTier", Codec.INTEGER, true),
                    (c, v) -> c.minTier = v, c -> c.minTier)
            .add()
            .append(new KeyedCodec<>("MaxTier", Codec.INTEGER, true),
                    (c, v) -> c.maxTier = v, c -> c.maxTier)
            .add()
            .append(new KeyedCodec<>("BlockDamageScale", Codec.FLOAT, true),
                    (c, v) -> c.blockDamageScale = v, c -> c.blockDamageScale)
            .add()
            .append(new KeyedCodec<>("TierBucketWidth", Codec.INTEGER, true),
                    (c, v) -> c.tierBucketWidth = v, c -> c.tierBucketWidth)
            .add()
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("ToolAssetPrefix", Codec.STRING, true),
                    (c, v) -> c.toolAssetPrefix = v, c -> c.toolAssetPrefix)
            .add()
            .build();
}
