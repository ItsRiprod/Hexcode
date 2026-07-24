package com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;

public class FortifyState implements ConstructState {

    private float remainingDuration;
    private String effectId;
    private List<String> nextGlyphIds;

    private boolean consumed;
    @Nullable
    private EntityVar attacker;

    public FortifyState() {
        this.nextGlyphIds = new ArrayList<>();
    }

    public FortifyState(float remainingDuration, String effectId, List<String> nextGlyphIds) {
        this.remainingDuration = remainingDuration;
        this.effectId = effectId;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public void setRemainingDuration(float remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    public String getEffectId() {
        return effectId;
    }

    public void tick(float dt) {
        remainingDuration -= dt;
    }

    public boolean isExpired() {
        return remainingDuration <= 0f;
    }

    public boolean isConsumed() {
        return consumed;
    }

    // attacker is null when the breaking hit had no entity source (fall, lava, explosion)
    public void consume(@Nullable EntityVar attacker) {
        this.consumed = true;
        this.attacker = attacker;
    }

    @Nullable
    public EntityVar getAttacker() {
        return attacker;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    public void refresh(float remainingDuration, List<String> nextGlyphIds) {
        this.remainingDuration = remainingDuration;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
        this.consumed = false;
        this.attacker = null;
    }

    @Override
    public FortifyState copy() {
        FortifyState c = new FortifyState(remainingDuration, effectId, new ArrayList<>(nextGlyphIds));
        c.consumed = this.consumed;
        c.attacker = this.attacker != null ? (EntityVar) this.attacker.copy() : null;
        return c;
    }
}
