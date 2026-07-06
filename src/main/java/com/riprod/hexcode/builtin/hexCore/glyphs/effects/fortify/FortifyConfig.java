package com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class FortifyConfig extends GlyphConfig {

    public static final FortifyConfig DEFAULTS = new FortifyConfig();

    private double minAmount = 1.0;
    private double maxAmount = 20.0;
    private double reductionScale = 0.5;
    private double blockHealScale = 0.05;
    private String effectId = "Hexcode_Fortify";

    public double getMinAmount() {
        return minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public double getReductionScale() {
        return reductionScale;
    }

    public double getBlockHealScale() {
        return blockHealScale;
    }

    public String getEffectId() {
        return effectId;
    }

    public static final BuilderCodec<FortifyConfig> CODEC = BuilderCodec
            .builder(FortifyConfig.class, FortifyConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("MinAmount", Codec.DOUBLE, true),
                    (c, v) -> c.minAmount = v, c -> c.minAmount)
            .add()
            .append(new KeyedCodec<>("MaxAmount", Codec.DOUBLE, true),
                    (c, v) -> c.maxAmount = v, c -> c.maxAmount)
            .add()
            .append(new KeyedCodec<>("ReductionScale", Codec.DOUBLE, true),
                    (c, v) -> c.reductionScale = v, c -> c.reductionScale)
            .add()
            .append(new KeyedCodec<>("BlockHealScale", Codec.DOUBLE, true),
                    (c, v) -> c.blockHealScale = v, c -> c.blockHealScale)
            .add()
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
