package com.riprod.hexcode.builtin.hexCore.glyphs.drain;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.execution.component.HexColors;

public class DrainState implements ConstructState {

    private int sourceStatIndex;
    @Nullable
    private Ref<EntityStore> destEntityRef;
    private float conversionRate;
    private float drainPerSecond;
    private float remainingSeconds;
    private float drainedSoFar;
    private List<String> nextGlyphIds;
    @Nullable
    private HexColors colors;

    public DrainState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public DrainState(int sourceStatIndex, @Nullable Ref<EntityStore> destEntityRef,
            float conversionRate, float totalDrainAmount, float durationSeconds,
            List<String> nextGlyphIds, @Nullable HexColors colors) {
        this.sourceStatIndex = sourceStatIndex;
        this.destEntityRef = destEntityRef;
        this.conversionRate = conversionRate;
        this.drainPerSecond = durationSeconds > 0 ? totalDrainAmount / durationSeconds : totalDrainAmount;
        this.remainingSeconds = durationSeconds;
        this.drainedSoFar = 0f;
        this.nextGlyphIds = nextGlyphIds;
        this.colors = colors;
    }

    public int getSourceStatIndex() {
        return sourceStatIndex;
    }

    @Nullable
    public Ref<EntityStore> getDestEntityRef() {
        return destEntityRef;
    }

    public float getConversionRate() {
        return conversionRate;
    }

    public float getDrainPerSecond() {
        return drainPerSecond;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public float getDrainedSoFar() {
        return drainedSoFar;
    }

    public void addDrained(float amount) {
        drainedSoFar += amount;
    }

    public void tick(float dt) {
        remainingSeconds -= dt;
    }

    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Nullable
    public HexColors getColors() {
        return colors;
    }

    @Override
    public DrainState copy() {
        DrainState c = new DrainState();
        c.sourceStatIndex = this.sourceStatIndex;
        c.destEntityRef = this.destEntityRef;
        c.conversionRate = this.conversionRate;
        c.drainPerSecond = this.drainPerSecond;
        c.remainingSeconds = this.remainingSeconds;
        c.drainedSoFar = this.drainedSoFar;
        c.nextGlyphIds = new ArrayList<>(this.nextGlyphIds);
        c.colors = this.colors;
        return c;
    }
}
