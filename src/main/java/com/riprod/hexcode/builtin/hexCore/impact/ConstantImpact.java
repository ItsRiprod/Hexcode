package com.riprod.hexcode.builtin.hexCore.impact;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class ConstantImpact extends Impact {
    public static final String ID = "Constant";

    private float value = 1.0f;

    @Override
    public float compute(double input) {
        return value;
    }

    public static final BuilderCodec<ConstantImpact> CODEC = BuilderCodec
            .builder(ConstantImpact.class, ConstantImpact::new, Impact.BASE_CODEC)
            .append(new KeyedCodec<>("Value", Codec.FLOAT),
                    (i, v) -> i.value = v, i -> i.value)
            .add()
            .build();
}
