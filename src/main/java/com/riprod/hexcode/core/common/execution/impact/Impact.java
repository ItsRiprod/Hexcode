package com.riprod.hexcode.core.common.execution.impact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;

public abstract class Impact {

    @Nonnull
    public static final CodecMapCodec<Impact> CODEC = new CodecMapCodec<>("Type");

    @Nonnull
    public static final BuilderCodec<Impact> BASE_CODEC = BuilderCodec.abstractBuilder(Impact.class).build();

    public abstract float compute(double input);

    public static float scale(@Nullable Impact impact, double input) {
        return impact == null ? 1.0f : Math.max(0.0f, impact.compute(input));
    }
}
