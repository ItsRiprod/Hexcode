package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class DelayConfig extends GlyphConfig {

    public static final DelayConfig DEFAULTS = new DelayConfig();

    private double gravity = 0.0;

    public double getGravity() {
        return gravity;
    }

    public static final BuilderCodec<DelayConfig> CODEC = BuilderCodec
            .builder(DelayConfig.class, DelayConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Gravity", Codec.DOUBLE, true),
                    (c, v) -> c.gravity = v, c -> c.gravity)
            .add()
            .build();
}
