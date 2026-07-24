package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.math.util.MathUtil;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class DelayState implements ConstructState {

    private float remainingSeconds;
    private List<String> nextGlyphIds;
    private boolean isCustom = false;

    public DelayState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public DelayState(float remainingSeconds, List<String> nextGlyphIds,
            boolean isCustom) {
        this.remainingSeconds = remainingSeconds;
        this.nextGlyphIds = nextGlyphIds;
        this.isCustom = isCustom;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public void tick(float dt) {
        if (remainingSeconds > 0f) {
            remainingSeconds = Math.max(0f, remainingSeconds - dt);
        }
    }

    public boolean isExpired() {
        return MathUtil.closeToZero(remainingSeconds);
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Nullable
    public boolean isCustom() {
        return isCustom;
    }

    @Override
    public DelayState copy() {
        return new DelayState(remainingSeconds, new ArrayList<>(nextGlyphIds), isCustom);
    }
}
