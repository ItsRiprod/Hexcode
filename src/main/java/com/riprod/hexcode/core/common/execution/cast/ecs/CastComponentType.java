package com.riprod.hexcode.core.common.execution.cast.ecs;

import javax.annotation.Nonnull;

public final class CastComponentType<T extends CastComponent> {

    private final Class<? super T> typeClass;
    private final int index;
    private final String id;

    public CastComponentType(@Nonnull Class<? super T> typeClass, int index, @Nonnull String id) {
        this.typeClass = typeClass;
        this.index = index;
        this.id = id;
    }

    @Nonnull
    public Class<? super T> getTypeClass() {
        return typeClass;
    }

    public int getIndex() {
        return index;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "CastComponentType[" + id + "@" + index + "]";
    }
}
