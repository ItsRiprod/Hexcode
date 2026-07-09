package com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class ErodeState implements ConstructState {

    private float remainingDuration;
    private String effectId;
    private List<String> nextGlyphIds;

    public ErodeState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public ErodeState(float remainingDuration, String effectId, List<String> nextGlyphIds) {
        this.remainingDuration = remainingDuration;
        this.effectId = effectId;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public void setRemainingDuration(float remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    public String getEffectId() {
        return effectId;
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
        return new ErodeState(remainingDuration, effectId, new ArrayList<>(nextGlyphIds));
    }
}
