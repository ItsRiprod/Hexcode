package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class GlyphBudgetComponent implements CastComponent {

    private long tick = -1L;
    private int spent;
    private int denied;

    public GlyphBudgetComponent() {
    }

    public boolean trySpend(long now, int allowance) {
        if (now != tick) {
            tick = now;
            spent = 0;
            denied = 0;
        }
        if (spent >= allowance) {
            denied++;
            return false;
        }
        spent++;
        return true;
    }

    public int getDenied() {
        return denied;
    }

    @Nonnull
    @Override
    public GlyphBudgetComponent copy() {
        GlyphBudgetComponent copy = new GlyphBudgetComponent();
        copy.tick = this.tick;
        copy.spent = this.spent;
        copy.denied = this.denied;
        return copy;
    }

    @Nonnull
    @Override
    public GlyphBudgetComponent branch() {
        return this;
    }

    public static final CastComponentType<GlyphBudgetComponent> TYPE = HexComponentRegistry.register(
            GlyphBudgetComponent.class, "GlyphBudget", a -> new GlyphBudgetComponent());
}
