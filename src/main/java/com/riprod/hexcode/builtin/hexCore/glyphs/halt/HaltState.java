package com.riprod.hexcode.builtin.hexCore.glyphs.halt;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class HaltState implements ConstructState {

    private float remainingDuration;
    @Nullable
    private PhysicsValues originalPhysicsValues;
    private List<String> nextGlyphIds;

    public HaltState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public HaltState(float remainingDuration, @Nullable PhysicsValues originalPhysicsValues,
            List<String> nextGlyphIds) {
        this.remainingDuration = remainingDuration;
        this.originalPhysicsValues = originalPhysicsValues;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public float getRemainingDuration() {
        return remainingDuration;
    }

    @Nullable
    public PhysicsValues getOriginalPhysicsValues() {
        return originalPhysicsValues;
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
    public HaltState copy() {
        PhysicsValues cloned = originalPhysicsValues != null
                ? new PhysicsValues(originalPhysicsValues) : null;
        return new HaltState(remainingDuration, cloned, new ArrayList<>(nextGlyphIds));
    }
}
