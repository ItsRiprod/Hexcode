package com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate;

import java.util.ArrayList;
import java.util.List;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class LevitateState implements ConstructState {

    private float appliedIntensity;
    private float remainingDuration;
    private float tickAccum;
    private List<String> nextGlyphIds;
    private String effectId;

    public LevitateState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public LevitateState(float appliedIntensity, float remainingDuration,
            List<String> nextGlyphIds) {
        this.appliedIntensity = appliedIntensity;
        this.remainingDuration = remainingDuration;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public float getAppliedIntensity() {
        return appliedIntensity;
    }

    public void setAppliedIntensity(float intensity) {
        this.appliedIntensity = intensity;
    }

    public float getRemainingDuration() {
        return remainingDuration;
    }

    public void setRemainingDuration(float remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    public float getTickAccum() {
        return tickAccum;
    }

    public void setTickAccum(float tickAccum) {
        this.tickAccum = tickAccum;
    }

    public String getEffectId() {
        return effectId;
    }

    public void setEffectId(String effectId) {
        this.effectId = effectId;
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
    public LevitateState copy() {
        LevitateState c = new LevitateState(appliedIntensity, remainingDuration,
                new ArrayList<>(nextGlyphIds));
        c.tickAccum = this.tickAccum;
        c.effectId = this.effectId;
        return c;
    }
}
