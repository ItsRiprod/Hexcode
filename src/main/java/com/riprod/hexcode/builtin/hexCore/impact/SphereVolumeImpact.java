package com.riprod.hexcode.builtin.hexCore.impact;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.riprod.hexcode.core.common.execution.impact.Impact;

public final class SphereVolumeImpact extends Impact {
    public static final String ID = "SphereVolume";

    private double defaultMagnitude = 1.0;
    private double exponent = 1.0;
    private float min = 1.0f;
    private float multiplier = 1.0f;
    @Nullable
    private Double clampMin;
    @Nullable
    private Double clampMax;

    @Override
    public float compute(double input) {
        double radius = input;
        if (clampMin != null)
            radius = Math.max(clampMin, radius);
        if (clampMax != null)
            radius = Math.min(clampMax, radius);
        double volume = (4.0 / 3.0) * Math.PI * radius * radius * radius;
        float curve = min;
        if (defaultMagnitude > 0.0) {
            double ratio = volume / defaultMagnitude;
            if (ratio > 1.0)
                curve = (float) Math.pow(ratio, exponent);
        }
        return multiplier * curve;
    }

    public static final BuilderCodec<SphereVolumeImpact> CODEC = BuilderCodec
            .builder(SphereVolumeImpact.class, SphereVolumeImpact::new, Impact.BASE_CODEC)
            .append(new KeyedCodec<>("DefaultMagnitude", Codec.DOUBLE),
                    (i, v) -> i.defaultMagnitude = v, i -> i.defaultMagnitude)
            .add()
            .append(new KeyedCodec<>("Exponent", Codec.DOUBLE),
                    (i, v) -> i.exponent = v, i -> i.exponent)
            .add()
            .append(new KeyedCodec<>("Min", Codec.FLOAT),
                    (i, v) -> i.min = v, i -> i.min)
            .add()
            .append(new KeyedCodec<>("Multiplier", Codec.FLOAT),
                    (i, v) -> i.multiplier = v, i -> i.multiplier)
            .add()
            .append(new KeyedCodec<>("ClampMin", Codec.DOUBLE),
                    (i, v) -> i.clampMin = v, i -> i.clampMin)
            .add()
            .append(new KeyedCodec<>("ClampMax", Codec.DOUBLE),
                    (i, v) -> i.clampMax = v, i -> i.clampMax)
            .add()
            .build();
}
