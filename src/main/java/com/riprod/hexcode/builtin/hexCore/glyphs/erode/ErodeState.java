package com.riprod.hexcode.builtin.hexCore.glyphs.erode;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class ErodeState implements ConstructState {

    private float vulnerabilityMultiplier;
    private float remainingDuration;
    private List<String> nextGlyphIds;

    public ErodeState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public ErodeState(float vulnerabilityMultiplier, float remainingDuration,
            List<String> nextGlyphIds) {
        this.vulnerabilityMultiplier = vulnerabilityMultiplier;
        this.remainingDuration = remainingDuration;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public float getVulnerabilityMultiplier() {
        return vulnerabilityMultiplier;
    }

    public void setVulnerabilityMultiplier(float vulnerabilityMultiplier) {
        this.vulnerabilityMultiplier = vulnerabilityMultiplier;
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
    public ErodeState copy() {
        return new ErodeState(vulnerabilityMultiplier, remainingDuration,
                new ArrayList<>(nextGlyphIds));
    }
}
