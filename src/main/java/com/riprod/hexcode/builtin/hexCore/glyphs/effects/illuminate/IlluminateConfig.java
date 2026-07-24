package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class IlluminateConfig extends GlyphConfig {

    public static final IlluminateConfig DEFAULTS = new IlluminateConfig();

    private int lightRadius = 8;
    private Color defaultColor = new Color((byte) 0, (byte) 204, (byte) 204);

    public int getLightRadius() {
        return lightRadius;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public static final BuilderCodec<IlluminateConfig> CODEC = BuilderCodec
            .builder(IlluminateConfig.class, IlluminateConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("LightRadius", Codec.INTEGER, true),
                    (c, v) -> c.lightRadius = v, c -> c.lightRadius)
            .add()
            .append(new KeyedCodec<>("DefaultColor", ProtocolCodecs.COLOR, true),
                    (c, v) -> c.defaultColor = v, c -> c.defaultColor)
            .add()
            .build();
}
