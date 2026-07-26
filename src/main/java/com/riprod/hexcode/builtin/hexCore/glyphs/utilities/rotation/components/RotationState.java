package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.components;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class RotationState implements ConstructState {

    private final float priorRoll;
    private float remainingSeconds;

    public RotationState(float priorRoll, float remainingSeconds) {
        this.priorRoll = priorRoll;
        this.remainingSeconds = remainingSeconds;
    }

    public float getPriorRoll() {
        return priorRoll;
    }

    public void setRemainingSeconds(float seconds) {
        this.remainingSeconds = seconds;
    }

    public void tick(float dt) {
        remainingSeconds -= dt;
    }

    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }

    @Override
    public RotationState copy() {
        return new RotationState(priorRoll, remainingSeconds);
    }
}
