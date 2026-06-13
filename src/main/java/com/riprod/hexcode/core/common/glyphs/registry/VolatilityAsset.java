package com.riprod.hexcode.core.common.glyphs.registry;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class VolatilityAsset {

    private float instantCost = 0.0f;
    // legacy default matching the old hardcoded dt*0.15f used in every construct handler
    private float drainPerSecond = 0.15f;
    private AreaTax areaTax = null;

    public VolatilityAsset() {
    }

    public float getInstantCost() {
        return instantCost;
    }

    public float getDrainPerSecond() {
        return drainPerSecond;
    }

    public AreaTax getAreaTax() {
        return areaTax;
    }

    public static class AreaTax {
        private float defaultMagnitude = 1.0f;
        private float exponent = 1.0f;

        public AreaTax() {
        }

        public float getDefaultMagnitude() {
            return defaultMagnitude;
        }

        public float getExponent() {
            return exponent;
        }

        public static final BuilderCodec<AreaTax> CODEC = BuilderCodec
                .builder(AreaTax.class, AreaTax::new)
                .append(new KeyedCodec<>("DefaultMagnitude", Codec.FLOAT),
                        (s, v) -> s.defaultMagnitude = v, s -> s.defaultMagnitude)
                .add()
                .append(new KeyedCodec<>("Exponent", Codec.FLOAT),
                        (s, v) -> s.exponent = v, s -> s.exponent)
                .add()
                .build();
    }

    public static final BuilderCodec<VolatilityAsset> CODEC = BuilderCodec
            .builder(VolatilityAsset.class, VolatilityAsset::new)
            .append(new KeyedCodec<>("InstantCost", Codec.FLOAT),
                    (s, v) -> s.instantCost = v, s -> s.instantCost)
            .add()
            .append(new KeyedCodec<>("DrainPerSecond", Codec.FLOAT),
                    (s, v) -> s.drainPerSecond = v, s -> s.drainPerSecond)
            .add()
            .append(new KeyedCodec<>("AreaTax", AreaTax.CODEC),
                    (s, v) -> s.areaTax = v, s -> s.areaTax)
            .add()
            .build();
}
