package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.cast.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;

public final class CastManaComponent implements CastComponent, CastOverlay<CastManaComponent> {

    private static final float UNSET = -1f;

    private static CastComponentType<CastManaComponent> componentType;

    public static CastComponentType<CastManaComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(CastComponentType<CastManaComponent> type) {
        componentType = type;
    }

    private float cost = UNSET;
    private float multiplier = 1.0f;

    public CastManaComponent() {
    }

    @Override
    public void applyTo(@Nonnull CastManaComponent target) {
        if (this.cost >= 0f) {
            target.setCost(this.cost);
        }
        target.setMultiplier(target.getMultiplier() * this.multiplier);
    }

    public float getCost() {
        return Math.max(0f, cost);
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public float getTotal() {
        return getCost() * multiplier;
    }

    @Nonnull
    @Override
    public CastManaComponent copy() {
        CastManaComponent copy = new CastManaComponent();
        copy.cost = this.cost;
        copy.multiplier = this.multiplier;
        return copy;
    }

    public static final BuilderCodec<CastManaComponent> CODEC = BuilderCodec
            .builder(CastManaComponent.class, CastManaComponent::new)
            .append(new KeyedCodec<>("Cost", Codec.FLOAT),
                    (c, v) -> c.cost = v,
                    c -> c.cost < 0f ? null : c.cost)
            .documentation("Replaces the mana cost computed from the hex.")
            .add()
            .append(new KeyedCodec<>("Multiplier", Codec.FLOAT),
                    (c, v) -> c.multiplier = v,
                    c -> c.multiplier)
            .documentation("Scales the mana cost. Compounds with earlier layers.")
            .add()
            .build();
}
