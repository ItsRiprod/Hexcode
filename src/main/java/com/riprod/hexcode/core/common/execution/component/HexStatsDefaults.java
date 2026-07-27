package com.riprod.hexcode.core.common.execution.component;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.ResourcePoolComponent;
import com.riprod.hexcode.core.common.execution.cast.VolatilityComponent;

public class HexStatsDefaults {

    @Nullable
    private Float initialVolatility;
    @Nullable
    private Float volatilityMultiplier;
    @Nullable
    private Float complexityMultiplier;
    @Nullable
    private Map<String, Float> initialResources;

    public HexStatsDefaults() {
    }

    @Nullable
    public Float getInitialVolatility() {
        return initialVolatility;
    }

    @Nullable
    public Float getVolatilityMultiplier() {
        return volatilityMultiplier;
    }

    @Nullable
    public Float getComplexityMultiplier() {
        return complexityMultiplier;
    }

    @Nullable
    public Map<String, Float> getInitialResources() {
        return initialResources;
    }

    public void applyTo(HexCast cast) {
        if (cast == null) return;
        VolatilityComponent volatility = cast.volatility();
        if (initialVolatility != null) {
            volatility.setInitial(initialVolatility);
            volatility.setCurrent(initialVolatility);
        }
        if (volatilityMultiplier != null) {
            volatility.setVolatilityMultiplier(volatility.getVolatilityMultiplier() * volatilityMultiplier);
        }
        if (complexityMultiplier != null) {
            volatility.setComplexityMultiplier(volatility.getComplexityMultiplier() * complexityMultiplier);
        }
        if (initialResources != null && !initialResources.isEmpty()) {
            ResourcePoolComponent pools = cast.mutableResources();
            initialResources.forEach(
                    (id, amount) -> pools.addResource(id, ResourcePoolComponent.SEED_SOURCE, amount));
        }
    }

    public static final BuilderCodec<HexStatsDefaults> CODEC = BuilderCodec
            .builder(HexStatsDefaults.class, HexStatsDefaults::new)
            .append(new KeyedCodec<>("InitialVolatility", Codec.FLOAT),
                    (c, v) -> c.initialVolatility = v,
                    c -> c.initialVolatility)
            .documentation("Volatility budget the cast starts with. Assigns, replacing any earlier layer.")
            .add()
            .append(new KeyedCodec<>("VolatilityMultiplier", Codec.FLOAT),
                    (c, v) -> c.volatilityMultiplier = v,
                    c -> c.volatilityMultiplier)
            .documentation("Scales volatility consumed per glyph. Compounds with earlier layers.")
            .add()
            .append(new KeyedCodec<>("ComplexityMultiplier", Codec.FLOAT),
                    (c, v) -> c.complexityMultiplier = v,
                    c -> c.complexityMultiplier)
            .documentation("Scales spell power. Compounds with earlier layers.")
            .add()
            .append(new KeyedCodec<>("InitialResources", new MapCodec<>(Codec.FLOAT, HashMap::new)),
                    (c, v) -> c.initialResources = v,
                    c -> c.initialResources)
            .documentation("Resource pools seeded before the first glyph runs.")
            .add()
            .build();
}
