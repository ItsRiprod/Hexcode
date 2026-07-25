package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class WardState implements ConstructState {

    private PersistentRef targetRef;
    private PersistentRef deferralRef;
    private float elapsedSeconds;
    private List<String> nextGlyphIds;

    public WardState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public WardState(PersistentRef targetRef, PersistentRef deferralRef, List<String> nextGlyphIds) {
        this.targetRef = targetRef;
        this.deferralRef = deferralRef;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public PersistentRef getTargetRef() {
        return targetRef;
    }

    public PersistentRef getDeferralRef() {
        return deferralRef;
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(float elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Override
    public WardState copy() {
        WardState c = new WardState(targetRef, deferralRef, new ArrayList<>(nextGlyphIds));
        c.elapsedSeconds = this.elapsedSeconds;
        return c;
    }
}
