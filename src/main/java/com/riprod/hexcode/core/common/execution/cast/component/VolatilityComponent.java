package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.cast.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;

public final class VolatilityComponent implements CastComponent, CastOverlay<VolatilityComponent> {

    private static CastComponentType<VolatilityComponent> componentType;

    public static CastComponentType<VolatilityComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(CastComponentType<VolatilityComponent> type) {
        componentType = type;
    }

    private float initial = -1f;
    private float current;
    private float volatilityMultiplier = 1.0f;
    private float complexityMultiplier = 1.0f;

    public VolatilityComponent() {
    }

    public void init(float initial, float volatilityMultiplier, float complexityMultiplier) {
        this.initial = initial;
        this.current = initial;
        this.volatilityMultiplier = volatilityMultiplier;
        this.complexityMultiplier = complexityMultiplier;
    }

    @Override
    public void applyTo(@Nonnull VolatilityComponent target) {
        if (this.initial >= 0f) {
            target.setInitial(this.initial);
            target.setCurrent(this.initial);
        }
        target.setVolatilityMultiplier(target.getVolatilityMultiplier() * this.volatilityMultiplier);
        target.setComplexityMultiplier(target.getComplexityMultiplier() * this.complexityMultiplier);
    }

    public float getInitial() {
        return Math.max(0f, initial);
    }

    public void setInitial(float initial) {
        this.initial = initial;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float value) {
        this.current = Math.max(0f, value);
    }

    public void add(float amount) {
        this.current = Math.max(0f, this.current + amount);
    }

    public float consume(float cost) {
        this.current = Math.max(0f, this.current - cost * volatilityMultiplier);
        return this.current;
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

    public void setComplexityMultiplier(float complexityMultiplier) {
        this.complexityMultiplier = complexityMultiplier;
    }

    @Nonnull
    @Override
    public VolatilityComponent copy() {
        VolatilityComponent copy = new VolatilityComponent();
        copy.initial = this.initial;
        copy.current = this.current;
        copy.volatilityMultiplier = this.volatilityMultiplier;
        copy.complexityMultiplier = this.complexityMultiplier;
        return copy;
    }

    public static final BuilderCodec<VolatilityComponent> CODEC = BuilderCodec
            .builder(VolatilityComponent.class, VolatilityComponent::new)
            .append(new KeyedCodec<>("Initial", Codec.FLOAT),
                    (c, v) -> c.initial = v,
                    c -> c.initial < 0f ? null : c.initial)
            .documentation("Volatility budget the cast starts with. Assigns, replacing any earlier layer.")
            .add()
            .append(new KeyedCodec<>("Multiplier", Codec.FLOAT),
                    (c, v) -> c.volatilityMultiplier = v,
                    c -> c.volatilityMultiplier)
            .documentation("Scales volatility consumed per glyph. Compounds with earlier layers.")
            .add()
            .append(new KeyedCodec<>("ComplexityMultiplier", Codec.FLOAT),
                    (c, v) -> c.complexityMultiplier = v,
                    c -> c.complexityMultiplier)
            .documentation("Scales spell power. Compounds with earlier layers.")
            .add()
            .build();
}
