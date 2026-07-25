package com.riprod.hexcode.builtin.hexCore.impact;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class LinearImpact extends Impact {
    public static final String ID = "Linear";

    private double slope = 1.0;
    private double offset = 0.0;
    private float min = -Float.MAX_VALUE;
    @Nullable
    private Double clampMin;
    @Nullable
    private Double clampMax;
    @Nullable
    private Double outputMin;
    @Nullable
    private Double outputMax;

    @Override
    public float compute(double input) {
        double value = input;
        if (clampMin != null)
            value = Math.max(clampMin, value);
        if (clampMax != null)
            value = Math.min(clampMax, value);
        double result = Math.max(min, offset + slope * value);
        if (outputMin != null)
            result = Math.max(outputMin, result);
        if (outputMax != null)
            result = Math.min(outputMax, result);
        return (float) result;
    }

    public static final BuilderCodec<LinearImpact> CODEC = BuilderCodec
            .builder(LinearImpact.class, LinearImpact::new, Impact.BASE_CODEC)
            .append(new KeyedCodec<>("Slope", Codec.DOUBLE),
                    (i, v) -> i.slope = v, i -> i.slope)
            .add()
            .append(new KeyedCodec<>("Offset", Codec.DOUBLE),
                    (i, v) -> i.offset = v, i -> i.offset)
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
            .append(new KeyedCodec<>("OutputMin", Codec.DOUBLE),
                    (i, v) -> i.outputMin = v, i -> i.outputMin)
            .add()
            .append(new KeyedCodec<>("OutputMax", Codec.DOUBLE),
                    (i, v) -> i.outputMax = v, i -> i.outputMax)
            .add()
            .build();
}
