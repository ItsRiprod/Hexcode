package com.riprod.hexcode.builtin.hexCore.impact;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class LinearImpact extends Impact {
    public static final String ID = "Linear";

    private double slope = 1.0;
    private double offset = 0.0;
    private float min = 0.0f;

    @Override
    public float compute(double input) {
        return Math.max(min, (float) (offset + slope * input));
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
            .build();
}
