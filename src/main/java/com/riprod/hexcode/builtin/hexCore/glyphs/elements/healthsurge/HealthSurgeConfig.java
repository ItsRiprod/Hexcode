package com.riprod.hexcode.builtin.hexCore.glyphs.elements.healthsurge;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class HealthSurgeConfig extends ElementGlyphConfig {

    public static final HealthSurgeConfig DEFAULTS = new HealthSurgeConfig();

    public static final BuilderCodec<HealthSurgeConfig> CODEC = BuilderCodec
            .builder(HealthSurgeConfig.class, HealthSurgeConfig::new, ElementGlyphConfig.BASE_CODEC)
            .build();
}
