package com.riprod.hexcode.builtin.hexCore.glyphs.domain;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class DomainConfig extends GlyphConfig {

    public static final String ID = "Domain";

    @Nullable
    private Impact triggerImpact;

    @Nullable
    public Impact getTriggerImpact() {
        return triggerImpact;
    }

    public static final BuilderCodec<DomainConfig> CODEC = BuilderCodec
            .builder(DomainConfig.class, DomainConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("TriggerImpact", Impact.CODEC),
                    (c, v) -> c.triggerImpact = v, c -> c.triggerImpact)
            .add()
            .build();
}
