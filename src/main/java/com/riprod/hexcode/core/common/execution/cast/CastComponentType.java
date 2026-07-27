package com.riprod.hexcode.core.common.execution.cast;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public final class CastComponentType<T extends CastComponent> {

    private final Class<T> typeClass;
    private final Supplier<T> supplier;
    private final int index;

    CastComponentType(@Nonnull Class<T> typeClass, @Nonnull Supplier<T> supplier, int index) {
        this.typeClass = typeClass;
        this.supplier = supplier;
        this.index = index;
    }

    @Nonnull
    public Class<T> getTypeClass() {
        return typeClass;
    }

    public int getIndex() {
        return index;
    }

    @Nonnull
    T create() {
        return supplier.get();
    }
}
