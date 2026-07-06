package com.riprod.hexcode.builtin.hexCore.glyphs.effects.drain;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class DrainConfig extends GlyphConfig {

    public static final DrainConfig DEFAULTS = new DrainConfig();

    private float hpToManaRate = 1.5f;
    private float staminaToManaRate = 0.6f;
    private float defaultConversionRate = 1.0f;
    private float defaultDrainPercent = 15.0f;
    private float hpFloor = 1.0f;
    private float durationFloor = 0.01f;

    public float getHpToManaRate() {
        return hpToManaRate;
    }

    public float getStaminaToManaRate() {
        return staminaToManaRate;
    }

    public float getDefaultConversionRate() {
        return defaultConversionRate;
    }

    public float getDefaultDrainPercent() {
        return defaultDrainPercent;
    }

    public float getHpFloor() {
        return hpFloor;
    }

    public float getDurationFloor() {
        return durationFloor;
    }

    public static final BuilderCodec<DrainConfig> CODEC = BuilderCodec
            .builder(DrainConfig.class, DrainConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("HpToManaRate", Codec.FLOAT, true),
                    (c, v) -> c.hpToManaRate = v, c -> c.hpToManaRate)
            .add()
            .append(new KeyedCodec<>("StaminaToManaRate", Codec.FLOAT, true),
                    (c, v) -> c.staminaToManaRate = v, c -> c.staminaToManaRate)
            .add()
            .append(new KeyedCodec<>("DefaultConversionRate", Codec.FLOAT, true),
                    (c, v) -> c.defaultConversionRate = v, c -> c.defaultConversionRate)
            .add()
            .append(new KeyedCodec<>("DefaultDrainPercent", Codec.FLOAT, true),
                    (c, v) -> c.defaultDrainPercent = v, c -> c.defaultDrainPercent)
            .add()
            .append(new KeyedCodec<>("HpFloor", Codec.FLOAT, true),
                    (c, v) -> c.hpFloor = v, c -> c.hpFloor)
            .add()
            .append(new KeyedCodec<>("DurationFloor", Codec.FLOAT, true),
                    (c, v) -> c.durationFloor = v, c -> c.durationFloor)
            .add()
            .build();
}
