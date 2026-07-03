package com.riprod.hexcode.builtin.hexCore.glyphs.levitate;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class LevitateConfig extends GlyphConfig {

    public static final LevitateConfig DEFAULTS = new LevitateConfig();

    private double durationFloor = 1.0;
    private float tickInterval = 0.5f;
    private double riseSpeedPerIntensity = 1.5;
    private String effectId = "Hexcode_Levitate";

    public double getDurationFloor() {
        return durationFloor;
    }

    public float getTickInterval() {
        return tickInterval;
    }

    public double getRiseSpeedPerIntensity() {
        return riseSpeedPerIntensity;
    }

    public String getEffectId() {
        return effectId;
    }

    public static final BuilderCodec<LevitateConfig> CODEC = BuilderCodec
            .builder(LevitateConfig.class, LevitateConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DurationFloor", Codec.DOUBLE, true),
                    (c, v) -> c.durationFloor = v, c -> c.durationFloor)
            .add()
            .append(new KeyedCodec<>("TickInterval", Codec.FLOAT, true),
                    (c, v) -> c.tickInterval = v, c -> c.tickInterval)
            .add()
            .append(new KeyedCodec<>("RiseSpeedPerIntensity", Codec.DOUBLE, true),
                    (c, v) -> c.riseSpeedPerIntensity = v, c -> c.riseSpeedPerIntensity)
            .add()
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
