package com.riprod.hexcode.builtin.hexCore.impact;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class RatioToDefaultImpact extends Impact {
    public static final String ID = "RatioToDefault";

    private double reference = 1.0;
    private float min = 1.0f;
    @Nullable
    private Double clampMin;
    @Nullable
    private Double clampMax;

    @Override
    public float compute(double input) {
        double value = input;
        if (clampMin != null)
            value = Math.max(clampMin, value);
        if (clampMax != null)
            value = Math.min(clampMax, value);
        double divisor = reference != 0.0 ? reference : 1.0;
        return (float) Math.max(min, value / divisor);
    }

    public static final BuilderCodec<RatioToDefaultImpact> CODEC = BuilderCodec
            .builder(RatioToDefaultImpact.class, RatioToDefaultImpact::new, Impact.BASE_CODEC)
            .append(new KeyedCodec<>("Reference", Codec.DOUBLE),
                    (i, v) -> i.reference = v, i -> i.reference)
            .add()
            .append(new KeyedCodec<>("Min", Codec.FLOAT),
                    (i, v) -> i.min = v, i -> i.min)
            .add()
            .append(new KeyedCodec<>("ClampMin", Codec.DOUBLE),
                    (i, v) -> i.clampMin = v, i -> i.clampMin)
            .add()
            .append(new KeyedCodec<>("ClampMax", Codec.DOUBLE),
                    (i, v) -> i.clampMax = v, i -> i.clampMax)
            .add()
            .build();
}
