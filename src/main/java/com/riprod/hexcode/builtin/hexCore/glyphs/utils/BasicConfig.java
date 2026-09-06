package com.riprod.hexcode.builtin.hexCore.glyphs.utils;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class BasicConfig extends GlyphConfig {

    public static final String ID = "Basic";

    public static final BuilderCodec<BasicConfig> CODEC = BuilderCodec
            .builder(BasicConfig.class, BasicConfig::new, GlyphConfig.BASE_CODEC)
            .build();
}
