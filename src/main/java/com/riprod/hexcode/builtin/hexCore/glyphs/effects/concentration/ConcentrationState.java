package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class ConcentrationState implements ConstructState {

    @Nullable
    private Ref<EntityStore> visualRef;

    private float tickAccum;

    private float elapsedSeconds;

    private float manaRate;

    private float staminaRate;

    private float healthRate;

    private float healthDamageAccum;

    private float bonusVolatilityPerSecond;

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

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(float elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public float getManaRate() {
        return manaRate;
    }

    public void setManaRate(float manaRate) {
        this.manaRate = manaRate;
    }

    public float getStaminaRate() {
        return staminaRate;
    }

    public void setStaminaRate(float staminaRate) {
        this.staminaRate = staminaRate;
    }

    public float getHealthRate() {
        return healthRate;
    }

    public void setHealthRate(float healthRate) {
        this.healthRate = healthRate;
    }

    public float getHealthDamageAccum() {
        return healthDamageAccum;
    }

    public void setHealthDamageAccum(float healthDamageAccum) {
        this.healthDamageAccum = healthDamageAccum;
    }

    public float getBonusVolatilityPerSecond() {
        return bonusVolatilityPerSecond;
    }

    public void setBonusVolatilityPerSecond(float bonusVolatilityPerSecond) {
        this.bonusVolatilityPerSecond = bonusVolatilityPerSecond;
    }

    @Override
    public ConcentrationState copy() {
        ConcentrationState c = new ConcentrationState(visualRef);
        c.tickAccum = this.tickAccum;
        c.elapsedSeconds = this.elapsedSeconds;
        c.manaRate = this.manaRate;
        c.staminaRate = this.staminaRate;
        c.healthRate = this.healthRate;
        c.healthDamageAccum = this.healthDamageAccum;
        c.bonusVolatilityPerSecond = this.bonusVolatilityPerSecond;
        return c;
    }
}
