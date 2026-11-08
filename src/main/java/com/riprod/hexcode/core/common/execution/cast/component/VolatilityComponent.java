package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class VolatilityComponent implements CastComponent, CastOverlay<VolatilityComponent> {

    private float initial = -1f;
    private float current;
    private float spent;

    public VolatilityComponent() {
    }

    public void init(float initial) {
        this.initial = initial;
        this.current = initial;
    }

    @Override
    public void applyTo(@Nonnull VolatilityComponent target) {
        if (this.initial >= 0f) {
            target.setInitial(this.initial);
            target.setCurrent(this.initial);
        }
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

    public float consume(float amount) {
        var remaining = Math.max(0f, this.current - amount);
        this.spent += Math.max(0f, this.current - remaining);
        this.current = remaining;
        return this.current;
    }

    public float getSpent() {
        return spent;
    }

    @Nonnull
    @Override
    public VolatilityComponent copy() {
        VolatilityComponent copy = new VolatilityComponent();
        copy.initial = this.initial;
        copy.current = this.current;
        copy.spent = this.spent;
        return copy;
    }

    @Nonnull
    @Override
    public VolatilityComponent branch() {
        return this;
    }

    public static final BuilderCodec<VolatilityComponent> CODEC = BuilderCodec
            .builder(VolatilityComponent.class, VolatilityComponent::new)
            .<Float>appendInherited(new KeyedCodec<>("Initial", Codec.FLOAT),
                    (c, v) -> c.initial = v,
                    c -> c.initial < 0f ? null : c.initial,
                    (c, p) -> c.initial = p.initial)
            .documentation("Volatility budget the cast starts with. Assigns, replacing any earlier layer.")
            .add()
            .build();

    public static final CastComponentType<VolatilityComponent> TYPE = HexComponentRegistry.register(
            VolatilityComponent.class, "Volatility", CODEC, a -> new VolatilityComponent());
}
