package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class ConcentrationState implements ConstructState {

    @Nullable
    private Ref<EntityStore> visualRef;

    private float tickAccum;

    private float volatilityBonus;

    public ConcentrationState() {
    }

    public ConcentrationState(@Nullable Ref<EntityStore> visualRef) {
        this.visualRef = visualRef;
    }

    @Nullable
    public Ref<EntityStore> getVisualRef() {
        return visualRef;
    }

    public float getTickAccum() {
        return tickAccum;
    }

    public void setTickAccum(float tickAccum) {
        this.tickAccum = tickAccum;
    }

    public float getVolatilityBonus() {
        return volatilityBonus;
    }

    public void setVolatilityBonus(float volatilityBonus) {
        this.volatilityBonus = volatilityBonus;
    }

    @Override
    public ConcentrationState copy() {
        ConcentrationState c = new ConcentrationState(visualRef);
        c.tickAccum = this.tickAccum;
        c.volatilityBonus = this.volatilityBonus;
        return c;
    }
}
