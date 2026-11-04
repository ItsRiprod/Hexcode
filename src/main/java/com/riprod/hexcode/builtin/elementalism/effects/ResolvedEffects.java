package com.riprod.hexcode.builtin.elementalism.effects;

public final class ResolvedEffects {
    private final EffectEntry[] entries;

    ResolvedEffects(EffectEntry[] entries) {
        this.entries = entries;
    }

    public EffectEntry get(EffectSlot slot) {
        return this.entries[slot.ordinal()];
    }
}
