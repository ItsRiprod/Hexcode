package com.riprod.hexcode.builtin.hexCore.glyphs.phase;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class PhaseState implements ConstructState {

    private List<String> nextGlyphIds;

    public PhaseState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public PhaseState(List<String> nextGlyphIds) {
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Override
    public PhaseState copy() {
        return new PhaseState(new ArrayList<>(nextGlyphIds));
    }
}
