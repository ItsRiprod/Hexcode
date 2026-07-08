package com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze.component.FrozenBlock;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class FreezeState implements ConstructState {

    private List<FrozenBlock> frozenBlocks;
    private String effectId;
    private float durationSeconds;
    private float elapsedSeconds;
    private List<String> nextGlyphIds;

    public FreezeState() {
        this.frozenBlocks = new ArrayList<>();
        this.nextGlyphIds = new ArrayList<>();
    }

    public FreezeState(List<FrozenBlock> frozenBlocks, String effectId, float durationSeconds,
            List<String> nextGlyphIds) {
        this.frozenBlocks = frozenBlocks;
        this.effectId = effectId;
        this.durationSeconds = durationSeconds;
        this.elapsedSeconds = 0f;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public List<FrozenBlock> getFrozenBlocks() {
        return frozenBlocks;
    }

    public String getEffectId() {
        return effectId;
    }

    public float getDurationSeconds() {
        return durationSeconds;
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
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
    public FreezeState copy() {
        FreezeState c = new FreezeState(new ArrayList<>(frozenBlocks), effectId, durationSeconds,
                new ArrayList<>(nextGlyphIds));
        c.elapsedSeconds = this.elapsedSeconds;
        return c;
    }
}
