package com.riprod.hexcode.builtin.elementalism.composite.action;

public interface CompositeAction {
    default boolean supportsEntity() {
        return false;
    }

    default boolean supportsBlock() {
        return false;
    }

    default boolean canApply(CompositeContext context) {
        return true;
    }

    void apply(CompositeContext context);
}
