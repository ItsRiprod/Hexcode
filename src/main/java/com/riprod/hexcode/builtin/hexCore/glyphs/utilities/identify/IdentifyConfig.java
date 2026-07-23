package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class IdentifyConfig extends GlyphConfig {

    public static final IdentifyConfig DEFAULTS = new IdentifyConfig();

    private String resourceId = "Life";
    private float durationPerLife = 1.0f;
    private float cap = -1f;

    public String getResourceId() {
        return resourceId;
    }

    public float getDurationPerLife() {
        return durationPerLife;
    }

    public float getCap() {
        return cap;
    }

    public static final BuilderCodec<IdentifyConfig> CODEC = BuilderCodec
            .builder(IdentifyConfig.class, IdentifyConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Resource", Codec.STRING, true),
                    (c, v) -> c.resourceId = v, c -> c.resourceId)
            .add()
            .append(new KeyedCodec<>("DurationPerLife", Codec.FLOAT, true),
                    (c, v) -> c.durationPerLife = v, c -> c.durationPerLife)
            .add()
            .append(new KeyedCodec<>("Cap", Codec.FLOAT, true),
                    (c, v) -> c.cap = v, c -> c.cap)
            .add()
            .build();
}
