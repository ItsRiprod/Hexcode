package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class ConcentrationState implements ConstructState {

    @Nullable
    private Ref<EntityStore> visualRef;

    private float tickAccum;

    private float volatilityBonus;

    @Nullable
    private PersistentRef targetRef;

    @Nullable
    private PersistentRef deferralRef;

    private boolean warded;

    private boolean upkeepActive;

    private float manaAccum;

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

    @Nullable
    public PersistentRef getTargetRef() {
        return targetRef;
    }

    public void setTargetRef(@Nullable PersistentRef targetRef) {
        this.targetRef = targetRef;
    }

    @Nullable
    public PersistentRef getDeferralRef() {
        return deferralRef;
    }

    public void setDeferralRef(@Nullable PersistentRef deferralRef) {
        this.deferralRef = deferralRef;
    }

    public boolean isWarded() {
        return warded;
    }

    public void setWarded(boolean warded) {
        this.warded = warded;
    }

    public boolean isUpkeepActive() {
        return upkeepActive;
    }

    public void setUpkeepActive(boolean upkeepActive) {
        this.upkeepActive = upkeepActive;
    }

    public float getManaAccum() {
        return manaAccum;
    }

    public void setManaAccum(float manaAccum) {
        this.manaAccum = manaAccum;
    }

    @Override
    public ConcentrationState copy() {
        ConcentrationState c = new ConcentrationState(visualRef);
        c.tickAccum = this.tickAccum;
        c.volatilityBonus = this.volatilityBonus;
        c.targetRef = this.targetRef;
        c.deferralRef = this.deferralRef;
        c.warded = this.warded;
        c.upkeepActive = this.upkeepActive;
        c.manaAccum = this.manaAccum;
        return c;
    }
}
