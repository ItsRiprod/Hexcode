package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.trilean;

import javax.annotation.Nullable;

public enum TrileanSlotState {
    NEGATIVE(-1),
    NEUTRAL(0),
    POSITIVE(1);

    private final int value;

    TrileanSlotState(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public TrileanSlotState cycle() {
        return switch (this) {
            case NEGATIVE -> NEUTRAL;
            case NEUTRAL -> POSITIVE;
            case POSITIVE -> NEGATIVE;
        };
    }

    public static TrileanSlotState fromDefault(@Nullable Double value) {
        if (value == null || value == 0.0) return NEUTRAL;
        return value < 0 ? NEGATIVE : POSITIVE;
    }

    public static TrileanSlotState fromOrdinal(int ordinal) {
        TrileanSlotState[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NEUTRAL;
    }
}
