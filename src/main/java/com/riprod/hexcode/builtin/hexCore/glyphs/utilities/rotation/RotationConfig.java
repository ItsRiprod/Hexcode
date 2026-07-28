package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class RotationConfig extends GlyphConfig {

    public static final RotationConfig DEFAULTS = new RotationConfig();

    private float rollHoldSeconds = 10.0f;
    private float playerVolatilityCost = 2.0f;

    public float getRollHoldSeconds() {
        return rollHoldSeconds;
    }

    public float getPlayerVolatilityCost() {
        return playerVolatilityCost;
    }

    public static final BuilderCodec<RotationConfig> CODEC = BuilderCodec
            .builder(RotationConfig.class, RotationConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("RollHoldSeconds", Codec.FLOAT, true),
                    (c, v) -> c.rollHoldSeconds = v, c -> c.rollHoldSeconds)
            .add()
            .append(new KeyedCodec<>("PlayerVolatilityCost", Codec.FLOAT, true),
                    (c, v) -> c.playerVolatilityCost = v, c -> c.playerVolatilityCost)
            .add()
            .build();
}
