package com.riprod.hexcode.builtin.hexCore.glyphs.levitate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.execution.component.HexColors;

public class LevitateState implements ConstructState {

    private float appliedIntensity;
    private float remainingDuration;
    private float tickAccum;
    @Nullable
    private HexColors colors;
    private List<String> nextGlyphIds;
    private String effectId;

    public LevitateState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public LevitateState(float appliedIntensity, float remainingDuration,
            @Nullable HexColors colors, List<String> nextGlyphIds) {
        this.appliedIntensity = appliedIntensity;
        this.remainingDuration = remainingDuration;
        this.colors = colors;
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

    @Nullable
    public HexColors getColors() {
        return colors;
    }

    public void setColors(@Nullable HexColors colors) {
        this.colors = colors;
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
                colors, new ArrayList<>(nextGlyphIds));
        c.tickAccum = this.tickAccum;
        c.effectId = this.effectId;
        return c;
    }
}
