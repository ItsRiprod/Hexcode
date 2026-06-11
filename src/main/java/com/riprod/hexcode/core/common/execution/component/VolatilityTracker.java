package com.riprod.hexcode.core.common.execution.component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class VolatilityTracker {
    private float startingBudget;
    private float remainingBudget;
    private float volatilityMultiplier;
    private float magicPowerMultiplier;
    private UUID executionId;
    private Map<String, Integer> glyphUsageMap = new HashMap<>();
    @javax.annotation.Nullable
    private String slotKey;

    public VolatilityTracker(float startingBudget, float volatilityMultiplier) {
        this(startingBudget, volatilityMultiplier, 1.0f);
    }

    public VolatilityTracker(float startingBudget, float volatilityMultiplier,
            float magicPowerMultiplier) {
        this.startingBudget = startingBudget;
        this.remainingBudget = startingBudget;
        this.volatilityMultiplier = volatilityMultiplier;
        this.magicPowerMultiplier = magicPowerMultiplier;
    }

    public VolatilityTracker() {
        this.startingBudget = 0f;
        this.remainingBudget = 0f;
        this.volatilityMultiplier = 1.0f;
        this.magicPowerMultiplier = 1.0f;
    }

    public static float computeGlyphCost(Glyph glyph) {
        return computeGlyphCost(glyph, 0);
    }

    public static float computeGlyphCost(Glyph glyph, int repeatCount) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        if (asset == null)
            return 0;
        float baseCost = asset.getVolatility().getCostForRepeat(repeatCount);
        // perfect draw (1.0) = 0.5x cost, worst draw (0.0) = 1.0x cost
        float qualityFactor = (1 - glyph.getVolatility()) * 0.5f + 0.5f;
        return baseCost * qualityFactor;
    }

    public boolean consumeVolatility(float cost) {
        float finalCost = cost * volatilityMultiplier;
        if (finalCost <= 0)
            return true;
        remainingBudget -= finalCost;
        if (remainingBudget <= 0) {
            remainingBudget = 0;
            return false;
        }
        return true;
    }

    public float getStartingBudget() {
        return startingBudget;
    }

    public void setStartingBudget(float startingBudget) {
        this.startingBudget = startingBudget;
    }

    public float getRemainingBudget() {
        return remainingBudget;
    }

    public void setBudget(float budget) {
        this.remainingBudget = Math.max(0f, budget);
    }

    public void addBudget(float budget) {
        this.remainingBudget = Math.max(0f, this.remainingBudget + budget);
    }

    public float getVolatilityMultiplier() {
        return volatilityMultiplier;
    }

    public void setVolatilityMultiplier(float volatilityMultiplier) {
        this.volatilityMultiplier = volatilityMultiplier;
    }

    public float getMagicPowerMultiplier() {
        return magicPowerMultiplier;
    }

    public void setMagicPowerMultiplier(float magicPowerMultiplier) {
        this.magicPowerMultiplier = magicPowerMultiplier;
    }

    public int getGlyphUsage(String glyphId) {
        return glyphUsageMap.getOrDefault(glyphId, 0);
    }

    public int incrementGlyphUsage(String glyphId) {
        int usage = getGlyphUsage(glyphId) + 1;
        glyphUsageMap.put(glyphId, usage);
        return usage;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public void setExecutionId(UUID executionId) {
        this.executionId = executionId;
    }

    @javax.annotation.Nullable
    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(@javax.annotation.Nullable String slotKey) {
        this.slotKey = slotKey;
    }

    public static final BuilderCodec<VolatilityTracker> CODEC = BuilderCodec
            .builder(VolatilityTracker.class, VolatilityTracker::new)
            .append(new KeyedCodec<>("StartingBudget", Codec.FLOAT),
                    (c, v) -> c.startingBudget = v,
                    (c) -> c.startingBudget)
            .add()
            .append(new KeyedCodec<>("RemainingBudget", Codec.FLOAT),
                    (c, v) -> c.remainingBudget = v,
                    (c) -> c.remainingBudget)
            .add()
            .append(new KeyedCodec<>("VolatilityMultiplier", Codec.FLOAT),
                    (c, v) -> c.volatilityMultiplier = v,
                    (c) -> c.volatilityMultiplier)
            .add()
            .append(new KeyedCodec<>("MagicPowerMultiplier", Codec.FLOAT),
                    (c, v) -> c.magicPowerMultiplier = v,
                    (c) -> c.magicPowerMultiplier)
            .add()
            .append(new KeyedCodec<>("ExecutionId", Codec.UUID_STRING),
                    (c, v) -> c.executionId = v,
                    (c) -> c.executionId)
            .add()
            .append(new KeyedCodec<>("SlotKey", Codec.STRING),
                    (c, v) -> c.slotKey = v,
                    (c) -> c.slotKey)
            .add()
            .build();

    public VolatilityTracker applyOverridesFrom(VolatilityTracker other) {
        if (other == null) return this;
        if (other.startingBudget > 0f) {
            this.startingBudget = other.startingBudget;
            this.remainingBudget = other.startingBudget;
        }
        if (other.volatilityMultiplier != 1.0f) {
            this.volatilityMultiplier *= other.volatilityMultiplier;
        }
        if (other.magicPowerMultiplier != 1.0f) {
            this.magicPowerMultiplier *= other.magicPowerMultiplier;
        }
        return this;
    }

    public VolatilityTracker copy() {
        VolatilityTracker copy = new VolatilityTracker();
        copy.startingBudget = this.startingBudget;
        copy.remainingBudget = this.remainingBudget;
        copy.volatilityMultiplier = this.volatilityMultiplier;
        copy.magicPowerMultiplier = this.magicPowerMultiplier;
        copy.executionId = this.executionId;
        copy.slotKey = this.slotKey;
        return copy;
    }
}
