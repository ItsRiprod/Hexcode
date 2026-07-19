package com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class DisguiseState implements ConstructState {

    private UUID constructId;
    private float remainingSeconds;
    private List<String> nextGlyphIds;

    public DisguiseState() {
        this.constructId = UUID.randomUUID();
        this.nextGlyphIds = new ArrayList<>();
    }

    public DisguiseState(UUID constructId, float remainingSeconds, List<String> nextGlyphIds) {
        this.constructId = constructId;
        this.remainingSeconds = remainingSeconds;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public UUID getConstructId() {
        return constructId;
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

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Override
    public DisguiseState copy() {
        return new DisguiseState(constructId, remainingSeconds, new ArrayList<>(nextGlyphIds));
    }
}
