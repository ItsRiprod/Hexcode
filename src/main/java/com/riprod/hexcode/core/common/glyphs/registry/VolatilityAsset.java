package com.riprod.hexcode.core.common.glyphs.registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class VolatilityAsset {

    private float instantCost = 0.0f;
    // legacy default matching the old hardcoded dt*0.15f used in every construct handler
    private float drainPerSecond = 0.15f;

    public VolatilityAsset() {
    }

    public float getInstantCost() {
        return instantCost;
    }

    public float getDrainPerSecond() {
        return drainPerSecond;
    }

    public static final BuilderCodec<VolatilityAsset> CODEC = BuilderCodec
            .builder(VolatilityAsset.class, VolatilityAsset::new)
            .append(new KeyedCodec<>("InstantCost", Codec.FLOAT),
                    (s, v) -> s.instantCost = v, s -> s.instantCost)
            .add()
            .append(new KeyedCodec<>("DrainPerSecond", Codec.FLOAT),
                    (s, v) -> s.drainPerSecond = v, s -> s.drainPerSecond)
            .add()
            .build();
}
