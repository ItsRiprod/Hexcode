package com.riprod.hexcode.builtin.hexCore.glyphs.fortify;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class FortifyState implements ConstructState {

    private float damageReduction;
    private float remainingDuration;
    private List<String> nextGlyphIds;

    public FortifyState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public FortifyState(float damageReduction, float remainingDuration,
            List<String> nextGlyphIds) {
        this.damageReduction = damageReduction;
        this.remainingDuration = remainingDuration;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public float getDamageReduction() {
        return damageReduction;
    }

    public void setDamageReduction(float damageReduction) {
        this.damageReduction = damageReduction;
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
    public FortifyState copy() {
        return new FortifyState(damageReduction, remainingDuration,
                new ArrayList<>(nextGlyphIds));
    }
}
