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

    private int resource;

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

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(float elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
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
        c.elapsedSeconds = this.elapsedSeconds;
        c.resource = this.resource;
        c.upkeepActive = this.upkeepActive;
        c.manaAccum = this.manaAccum;
        return c;
    }
}
