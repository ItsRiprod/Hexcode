package com.riprod.hexcode.builtin.hexCore.glyphs.shatter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ShatterConfig extends GlyphConfig {

    public static final ShatterConfig DEFAULTS = new ShatterConfig();

    private int defaultCount = 5;
    private double defaultSpread = Math.PI / 6;
    private double defaultSpeed = 20.0;
    private double defaultGravity = 10.0;
    private int maxCount = 16;
    private double shardTtlSeconds = 600.0;

    public int getDefaultCount() {
        return defaultCount;
    }

    public double getDefaultSpread() {
        return defaultSpread;
    }

    public double getDefaultSpeed() {
        return defaultSpeed;
    }

    public double getDefaultGravity() {
        return defaultGravity;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public double getShardTtlSeconds() {
        return shardTtlSeconds;
    }

    public static final BuilderCodec<ShatterConfig> CODEC = BuilderCodec
            .builder(ShatterConfig.class, ShatterConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DefaultCount", Codec.INTEGER, true),
                    (c, v) -> c.defaultCount = v, c -> c.defaultCount)
            .add()
            .append(new KeyedCodec<>("DefaultSpread", Codec.DOUBLE, true),
                    (c, v) -> c.defaultSpread = v, c -> c.defaultSpread)
            .add()
            .append(new KeyedCodec<>("DefaultSpeed", Codec.DOUBLE, true),
                    (c, v) -> c.defaultSpeed = v, c -> c.defaultSpeed)
            .add()
            .append(new KeyedCodec<>("DefaultGravity", Codec.DOUBLE, true),
                    (c, v) -> c.defaultGravity = v, c -> c.defaultGravity)
            .add()
            .append(new KeyedCodec<>("MaxCount", Codec.INTEGER, true),
                    (c, v) -> c.maxCount = v, c -> c.maxCount)
            .add()
            .append(new KeyedCodec<>("ShardTtlSeconds", Codec.DOUBLE, true),
                    (c, v) -> c.shardTtlSeconds = v, c -> c.shardTtlSeconds)
            .add()
            .build();
}
