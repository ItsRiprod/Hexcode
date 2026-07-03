package com.riprod.hexcode.builtin.hexCore.glyphs.beam;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class BeamConfig extends GlyphConfig {

    public static final BeamConfig DEFAULTS = new BeamConfig();

    private double originOffset = 1.5;

    public double getOriginOffset() {
        return originOffset;
    }

    public static final BuilderCodec<BeamConfig> CODEC = BuilderCodec
            .builder(BeamConfig.class, BeamConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("OriginOffset", Codec.DOUBLE, true),
                    (c, v) -> c.originOffset = v, c -> c.originOffset)
            .add()
            .build();
}
