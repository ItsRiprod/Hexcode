package com.riprod.hexcode.builtin.hexCore.glyphs.number;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class NumberConfig extends GlyphConfig {

    public static final NumberConfig DEFAULTS = new NumberConfig();

    private double value = 0.0;

    public double getValue() {
        return value;
    }

    public static final BuilderCodec<NumberConfig> CODEC = BuilderCodec
            .builder(NumberConfig.class, NumberConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Value", Codec.DOUBLE, true),
                    (c, v) -> c.value = v, c -> c.value)
            .add()
            .build();
}
