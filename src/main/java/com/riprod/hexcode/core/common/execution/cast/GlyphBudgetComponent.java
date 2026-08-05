package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;

public final class GlyphBudgetComponent implements CastComponent {

    private static CastComponentType<GlyphBudgetComponent> componentType;

    public static CastComponentType<GlyphBudgetComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(CastComponentType<GlyphBudgetComponent> type) {
        componentType = type;
    }

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
}
