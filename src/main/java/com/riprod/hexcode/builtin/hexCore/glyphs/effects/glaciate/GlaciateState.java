package com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class GlaciateState implements ConstructState {

    private List<String> nextGlyphIds;

    public GlaciateState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public GlaciateState(List<String> nextGlyphIds) {
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Override
    public GlaciateState copy() {
        return new GlaciateState(new ArrayList<>(nextGlyphIds));
    }
}
