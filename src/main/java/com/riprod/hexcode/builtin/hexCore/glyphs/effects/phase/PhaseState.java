package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.execution.impact.Impact;

public class PhaseState implements ConstructState {

    private List<String> nextGlyphIds;
    @Nullable
    private Impact crushDamageImpact;
    private String damageCauseId;

    public PhaseState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public PhaseState(List<String> nextGlyphIds, @Nullable Impact crushDamageImpact, String damageCauseId) {
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
        this.crushDamageImpact = crushDamageImpact;
        this.damageCauseId = damageCauseId;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Nullable
    public Impact getCrushDamageImpact() {
        return crushDamageImpact;
    }

    public String getDamageCauseId() {
        return damageCauseId;
    }

    @Override
    public PhaseState copy() {
        return new PhaseState(new ArrayList<>(nextGlyphIds), crushDamageImpact, damageCauseId);
    }
}
