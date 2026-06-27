package com.riprod.hexcode.core.common.glyphs.registry;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.riprod.hexcode.core.common.execution.impact.Impact;

public abstract class GlyphConfig {

    public static final CodecMapCodec<GlyphConfig> CODEC = new CodecMapCodec<>("Type");

    public static final BuilderCodec<GlyphConfig> BASE_CODEC = BuilderCodec
            .abstractBuilder(GlyphConfig.class)
            .append(new KeyedCodec<>("Impact", Impact.CODEC),
                    (c, v) -> c.impact = v, c -> c.impact)
            .add()
            .build();

    @Nullable
    protected Impact impact;

    @Nullable
    public Impact getImpact() {
        return impact;
    }
}
