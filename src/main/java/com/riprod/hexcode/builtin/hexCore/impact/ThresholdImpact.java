package com.riprod.hexcode.builtin.hexCore.impact;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class ThresholdImpact extends Impact {
    public static final String ID = "Threshold";

    private double reference = 1.0;
    private double threshold = 1.0;
    private double exponent = 1.0;

    @Override
    public float compute(double input) {
        double value = Math.max(0.0, input);
        double ratio = value / reference;
        if (value <= threshold)
            return (float) ratio;
        double base = threshold / reference;
        double excess = (value - threshold) / reference;
        return (float) (base + excess * Math.pow(value / threshold, exponent));
    }

    public static final BuilderCodec<ThresholdImpact> CODEC = BuilderCodec
            .builder(ThresholdImpact.class, ThresholdImpact::new, Impact.BASE_CODEC)
            .append(new KeyedCodec<>("Reference", Codec.DOUBLE),
                    (i, v) -> i.reference = v, i -> i.reference)
            .add()
            .append(new KeyedCodec<>("Threshold", Codec.DOUBLE),
                    (i, v) -> i.threshold = v, i -> i.threshold)
            .add()
            .append(new KeyedCodec<>("Exponent", Codec.DOUBLE),
                    (i, v) -> i.exponent = v, i -> i.exponent)
            .add()
            .build();
}
