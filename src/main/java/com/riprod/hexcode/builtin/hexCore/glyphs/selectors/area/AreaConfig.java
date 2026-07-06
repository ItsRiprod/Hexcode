package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class AreaConfig extends GlyphConfig {

    public static final AreaConfig DEFAULTS = new AreaConfig();

    private double particleMargin = 25.0;

    public double getParticleMargin() {
        return particleMargin;
    }

    public static final BuilderCodec<AreaConfig> CODEC = BuilderCodec
            .builder(AreaConfig.class, AreaConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ParticleMargin", Codec.DOUBLE, true),
                    (c, v) -> c.particleMargin = v, c -> c.particleMargin)
            .add()
            .build();
}
