package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class WardConfig extends GlyphConfig {

    public static final WardConfig DEFAULTS = new WardConfig();

    public static final float DEFAULT_SUSTAIN_PER_SECOND = -4.0f;

    @Nullable
    private Impact sustainImpact;

    @Nullable
    public Impact getSustainImpact() {
        return sustainImpact;
    }

    public static final BuilderCodec<WardConfig> CODEC = BuilderCodec
            .builder(WardConfig.class, WardConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("SustainImpact", Impact.CODEC, true),
                    (c, v) -> c.sustainImpact = v, c -> c.sustainImpact)
            .add()
            .build();
}
