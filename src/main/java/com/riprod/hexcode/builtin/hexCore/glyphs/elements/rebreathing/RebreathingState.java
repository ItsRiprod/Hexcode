package com.riprod.hexcode.builtin.hexCore.glyphs.elements.rebreathing;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class RebreathingState implements ConstructState {

    private String effectId;
    private float durationSeconds;
    private float elapsedSeconds;
    private List<String> nextGlyphIds;

    public RebreathingState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public RebreathingState(String effectId, float durationSeconds, List<String> nextGlyphIds) {
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

    @Override
    public RebreathingState copy() {
        RebreathingState c = new RebreathingState(effectId, durationSeconds, new ArrayList<>(nextGlyphIds));
        c.elapsedSeconds = this.elapsedSeconds;
        return c;
    }
}
