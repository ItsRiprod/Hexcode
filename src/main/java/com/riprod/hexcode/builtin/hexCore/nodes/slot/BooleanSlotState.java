package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import javax.annotation.Nullable;

public enum BooleanSlotState {
    NEGATIVE(-1),
    NEUTRAL(0),
    POSITIVE(1);

    private final int value;

    BooleanSlotState(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public BooleanSlotState cycle() {
        return switch (this) {
            case NEGATIVE -> NEUTRAL;
            case NEUTRAL -> POSITIVE;
            case POSITIVE -> NEGATIVE;
        };
    }

    public static BooleanSlotState fromDefault(@Nullable Double value) {
        if (value == null || value == 0.0) return NEUTRAL;
        return value < 0 ? NEGATIVE : POSITIVE;
    }

    public static BooleanSlotState fromOrdinal(int ordinal) {
        BooleanSlotState[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NEUTRAL;
    }
}
