package com.riprod.hexcode.builtin.hexCore.glyphs.arc;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ArcConfig extends GlyphConfig {

    public static final ArcConfig DEFAULTS = new ArcConfig();

    private double shockOverlapSeconds = 0.25;
    private String effectId = "Hexcode_Shock";

    public double getShockOverlapSeconds() {
        return shockOverlapSeconds;
    }

    public String getEffectId() {
        return effectId;
    }

    public static final BuilderCodec<ArcConfig> CODEC = BuilderCodec
            .builder(ArcConfig.class, ArcConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ShockOverlapSeconds", Codec.DOUBLE, true),
                    (c, v) -> c.shockOverlapSeconds = v, c -> c.shockOverlapSeconds)
            .add()
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
