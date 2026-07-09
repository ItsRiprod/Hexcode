package com.riprod.hexcode.core.common.glyphs.registry;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.riprod.hexcode.core.common.execution.impact.Impact;

public abstract class GlyphConfig {

    public static final Impact DEFAULT_COMPLEXITY_IMPACT = new Impact() {
        @Override
        public float compute(double input) {
            return 1.3f;
        }
    };

    public static final CodecMapCodec<GlyphConfig> CODEC = new CodecMapCodec<>("Type");

    public static final BuilderCodec<GlyphConfig> BASE_CODEC = BuilderCodec
            .abstractBuilder(GlyphConfig.class)
            .append(new KeyedCodec<>("VolatilityImpact", Impact.CODEC),
                    (c, v) -> c.volatilityImpact = v, c -> c.volatilityImpact)
            .add()
            .append(new KeyedCodec<>("ComplexityImpact", Impact.CODEC, true),
                    (c, v) -> c.complexityImpact = v, c -> c.complexityImpact)
            .add()
            .build();

    @Nullable
    protected Impact volatilityImpact;

    protected Impact complexityImpact = DEFAULT_COMPLEXITY_IMPACT;

    @Nullable
    public Impact getVolatilityImpact() {
        return volatilityImpact;
    }

    public Impact getComplexityImpact() {
        return complexityImpact;
    }
}
