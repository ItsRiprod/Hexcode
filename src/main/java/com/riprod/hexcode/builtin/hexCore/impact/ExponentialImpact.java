package com.riprod.hexcode.builtin.hexCore.impact;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class ExponentialImpact extends Impact {
    public static final String ID = "Exponential";

    private double growth = 1.0;
    private float min = 1.0f;

    @Override
    public float compute(double input) {
        return Math.max(min, (float) Math.expm1(growth * (input - 1.0)));
    }

    public static final BuilderCodec<ExponentialImpact> CODEC = BuilderCodec
            .builder(ExponentialImpact.class, ExponentialImpact::new, Impact.BASE_CODEC)
            .append(new KeyedCodec<>("Growth", Codec.DOUBLE),
                    (i, v) -> i.growth = v, i -> i.growth)
            .add()
            .append(new KeyedCodec<>("Min", Codec.FLOAT),
                    (i, v) -> i.min = v, i -> i.min)
            .add()
            .build();
}
