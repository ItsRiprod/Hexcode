package com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class ErodeState implements ConstructState {

    private float vulnerabilityMultiplier;
    private float remainingDuration;
    private List<String> nextGlyphIds;
    private String effectId;

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

    public String getEffectId() {
        return effectId;
    }

    public void setEffectId(String effectId) {
        this.effectId = effectId;
    }

    @Override
    public ErodeState copy() {
        ErodeState c = new ErodeState(vulnerabilityMultiplier, remainingDuration,
                new ArrayList<>(nextGlyphIds));
        c.effectId = this.effectId;
        return c;
    }
}
