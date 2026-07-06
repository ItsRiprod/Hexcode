package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class PhaseState implements ConstructState {

    private List<String> nextGlyphIds;
    private float crushDamage;
    private String damageCauseId;

    public PhaseState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public PhaseState(List<String> nextGlyphIds, float crushDamage, String damageCauseId) {
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
        this.crushDamage = crushDamage;
        this.damageCauseId = damageCauseId;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    public float getCrushDamage() {
        return crushDamage;
    }

    public String getDamageCauseId() {
        return damageCauseId;
    }

    @Override
    public PhaseState copy() {
        return new PhaseState(new ArrayList<>(nextGlyphIds), crushDamage, damageCauseId);
    }
}
