package com.riprod.hexcode.core.common.execution.component;

import java.util.UUID;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

public class HexStats {
    private float manaCost;
    private float manaMultiplier;
    private float initialVolatility;
    private float currentVolatility;
    private float volatilityMultiplier;
    private float complexityMultiplier;
    private final transient Object2FloatOpenHashMap<String> resources = new Object2FloatOpenHashMap<>();
    private final transient Object2FloatOpenHashMap<String> contributed = new Object2FloatOpenHashMap<>();
    private UUID executionId;
    private transient String slotKey;

    public HexStats() {
    }

    public HexStats(float initialVolatility, float volatilityMultiplier, float complexityMultiplier) {
        this.initialVolatility = initialVolatility;
        this.currentVolatility = initialVolatility;
        this.volatilityMultiplier = volatilityMultiplier;
        this.complexityMultiplier = complexityMultiplier;
    }

    public float getInitialVolatility() {
        return initialVolatility;
    }

    public void setInitialVolatility(float startingBudget) {
        this.initialVolatility = startingBudget;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
    }

    public float getCurrentVolatility() {
        return currentVolatility;
    }

    public void setVolatility(float budget) {
        this.currentVolatility = Math.max(0f, budget);
    }

    public void addVolatility(float budget) {
        this.currentVolatility = Math.max(0f, this.currentVolatility + budget);
    }

    public float consumeVolatility(float cost) {
        this.currentVolatility = Math.max(0f, this.currentVolatility - cost * volatilityMultiplier);
        return this.currentVolatility;
    }

    public float getResource(String id) {
        return this.resources.getFloat(id);
    }

    public void addResource(String id, float amount) {
        this.resources.put(id, Math.max(0f, this.resources.getFloat(id) + amount));
    }

    public float consumeResource(String id, float cap) {
        float pooled = this.resources.getFloat(id);
        float taken = cap < 0f ? pooled : Math.min(pooled, cap);
        if (taken <= 0f) return 0f;
        this.resources.put(id, pooled - taken);
        return taken;
    }

    public float getContributed(String id) {
        return this.contributed.getFloat(id);
    }

    public void addContributed(String id, float amount) {
        this.contributed.put(id, this.contributed.getFloat(id) + amount);
    }

    public Object2FloatOpenHashMap<String> getResources() {
        return this.resources;
    }

    public float getVolatilityMultiplier() {
        return volatilityMultiplier;
    }

    public void setVolatilityMultiplier(float volatilityMultiplier) {
        this.volatilityMultiplier = volatilityMultiplier;
    }

    public float getComplexityMultiplier() {
        return complexityMultiplier;
    }

    public void setComplexityMultiplier(float magicPowerMultiplier) {
        this.complexityMultiplier = magicPowerMultiplier;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public void setExecutionId(UUID executionId) {
        this.executionId = executionId;
    }

    public static final BuilderCodec<HexStats> CODEC = BuilderCodec
            .builder(HexStats.class, HexStats::new)
            .append(new KeyedCodec<>("ManaCost", Codec.FLOAT),
                    (c, v) -> c.manaCost = v,
                    (c) -> c.manaCost)
            .add()
            .append(new KeyedCodec<>("ManaMultiplier", Codec.FLOAT),
                    (c, v) -> c.manaMultiplier = v,
                    (c) -> c.manaMultiplier)
            .add()
            .append(new KeyedCodec<>("InitialVolatility", Codec.FLOAT),
                    (c, v) -> c.initialVolatility = v,
                    (c) -> c.initialVolatility)
            .add()
            .append(new KeyedCodec<>("CurrentVolatility", Codec.FLOAT),
                    (c, v) -> c.currentVolatility = v,
                    (c) -> c.currentVolatility)
            .metadata(UIDisplayMode.HIDDEN)
            .add()
            .append(new KeyedCodec<>("VolatilityMultiplier", Codec.FLOAT),
                    (c, v) -> c.volatilityMultiplier = v,
                    (c) -> c.volatilityMultiplier)
            .add()
            .append(new KeyedCodec<>("ComplexityMultiplier", Codec.FLOAT),
                    (c, v) -> c.complexityMultiplier = v,
                    (c) -> c.complexityMultiplier)
            .add()
            .append(new KeyedCodec<>("ExecutionId", Codec.UUID_STRING),
                    (c, v) -> c.executionId = v,
                    (c) -> c.executionId)
            .add()
            .metadata(UIDisplayMode.HIDDEN)
            .build();

    public HexStats applyOverridesFrom(HexStats other) {
        if (other == null)
            return this;
        if (other.initialVolatility > 0f) {
            this.initialVolatility = other.initialVolatility;
            this.currentVolatility = other.initialVolatility;
        }

        if (other.volatilityMultiplier != 1.0f) {
            this.volatilityMultiplier *= other.volatilityMultiplier;
        }
        if (other.complexityMultiplier != 1.0f) {
            this.complexityMultiplier *= other.complexityMultiplier;
        }
        return this;
    }

    public HexStats copy() {
        HexStats copy = new HexStats();
        copy.initialVolatility = this.initialVolatility;
        copy.currentVolatility = this.currentVolatility;
        copy.volatilityMultiplier = this.volatilityMultiplier;
        copy.complexityMultiplier = this.complexityMultiplier;
        copy.resources.putAll(this.resources);
        copy.contributed.putAll(this.contributed);
        copy.executionId = this.executionId;
        return copy;
    }
}
