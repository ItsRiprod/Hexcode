package com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ScaleConfig extends GlyphConfig {

    public static final ScaleConfig DEFAULTS = new ScaleConfig();

    private double minMagnitude = 0.01;
    private double maxMagnitude = 32.0;
    private double minDuration = 0.1;
    private float mountOffsetY = 2.5f;

    public double getMinMagnitude() {
        return minMagnitude;
    }

    public double getMaxMagnitude() {
        return maxMagnitude;
    }

    public double getMinDuration() {
        return minDuration;
    }

    public float getMountOffsetY() {
        return mountOffsetY;
    }

    public static final BuilderCodec<ScaleConfig> CODEC = BuilderCodec
            .builder(ScaleConfig.class, ScaleConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("MinMagnitude", Codec.DOUBLE, true),
                    (c, v) -> c.minMagnitude = v, c -> c.minMagnitude)
            .add()
            .append(new KeyedCodec<>("MaxMagnitude", Codec.DOUBLE, true),
                    (c, v) -> c.maxMagnitude = v, c -> c.maxMagnitude)
            .add()
            .append(new KeyedCodec<>("MinDuration", Codec.DOUBLE, true),
                    (c, v) -> c.minDuration = v, c -> c.minDuration)
            .add()
            .append(new KeyedCodec<>("MountOffsetY", Codec.FLOAT, true),
                    (c, v) -> c.mountOffsetY = v, c -> c.mountOffsetY)
            .add()
            .build();
}
