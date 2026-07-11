package com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class FreezeState implements ConstructState {

    private String effectId;
    private float durationSeconds;
    private float elapsedSeconds;
    private List<String> nextGlyphIds;
    private boolean cleanedUp;

    public FreezeState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public FreezeState(String effectId, float durationSeconds, List<String> nextGlyphIds) {
        this.effectId = effectId;
        this.durationSeconds = durationSeconds;
        this.elapsedSeconds = 0f;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public String getEffectId() {
        return effectId;
    }

    public void tick(float dt) {
        elapsedSeconds += dt;
    }

    public boolean isExpired() {
        return elapsedSeconds >= durationSeconds;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    public boolean isCleanedUp() {
        return cleanedUp;
    }

    public void markCleanedUp() {
        this.cleanedUp = true;
    }

    public void refresh(float durationSeconds, List<String> nextGlyphIds) {
        this.durationSeconds = durationSeconds;
        this.elapsedSeconds = 0f;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
        this.cleanedUp = false;
    }

    @Override
    public FreezeState copy() {
        FreezeState c = new FreezeState(effectId, durationSeconds, new ArrayList<>(nextGlyphIds));
        c.elapsedSeconds = this.elapsedSeconds;
        c.cleanedUp = this.cleanedUp;
        return c;
    }
}
