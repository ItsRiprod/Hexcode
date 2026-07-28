package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;

public final class VolatilityComponent implements CastComponent {

    private static CastComponentType<VolatilityComponent> componentType;

    public static CastComponentType<VolatilityComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(CastComponentType<VolatilityComponent> type) {
        componentType = type;
    }

    private float initial;
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

    public float getInitial() {
        return initial;
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
}
