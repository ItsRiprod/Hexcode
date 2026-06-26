package com.riprod.hexcode.builtin.hexCore.glyphs.growth;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class GrowthState implements ConstructState {

    private float remainingDuration;
    private List<String> nextGlyphIds;

    public GrowthState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public GrowthState(float remainingDuration, List<String> nextGlyphIds) {
        this.remainingDuration = remainingDuration;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public float getRemainingDuration() {
        return remainingDuration;
    }

    public void setRemainingDuration(float remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    public void tick(float dt) {
        remainingDuration -= dt;
    }

    public boolean isExpired() {
        return remainingDuration <= 0f;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Override
    public GrowthState copy() {
        return new GrowthState(remainingDuration, new ArrayList<>(nextGlyphIds));
    }
}
