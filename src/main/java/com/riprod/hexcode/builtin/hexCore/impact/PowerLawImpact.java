package com.riprod.hexcode.builtin.hexCore.impact;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class PowerLawImpact extends Impact {
    public static final String ID = "PowerLaw";

    private double defaultMagnitude = 1.0;
    private double exponent = 1.0;
    private float min = 1.0f;

    @Override
    public float compute(double input) {
        if (defaultMagnitude <= 0.0)
            return min;
        double ratio = input / defaultMagnitude;
        return (float) Math.max(Math.pow(ratio, exponent), min);
    }

    public static final BuilderCodec<PowerLawImpact> CODEC = BuilderCodec
            .builder(PowerLawImpact.class, PowerLawImpact::new, Impact.BASE_CODEC)
            .append(new KeyedCodec<>("DefaultMagnitude", Codec.DOUBLE),
                    (i, v) -> i.defaultMagnitude = v, i -> i.defaultMagnitude)
            .add()
            .append(new KeyedCodec<>("Exponent", Codec.DOUBLE),
                    (i, v) -> i.exponent = v, i -> i.exponent)
            .add()
            .append(new KeyedCodec<>("Min", Codec.FLOAT),
                    (i, v) -> i.min = v, i -> i.min)
            .add()
            .build();
}
