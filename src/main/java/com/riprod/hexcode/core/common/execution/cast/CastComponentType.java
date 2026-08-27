package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;

public final class CastComponentType<T extends CastComponent> {

    private CastComponentRegistry registry;
    private Class<? super T> typeClass;
    private int index;
    private boolean valid;

    CastComponentType() {
    }

    void init(@Nonnull CastComponentRegistry registry, @Nonnull Class<? super T> typeClass, int index) {
        this.registry = registry;
        this.typeClass = typeClass;
        this.index = index;
        this.valid = true;
    }

    void invalidate() {
        this.valid = false;
    }

    @Nonnull
    public CastComponentRegistry getRegistry() {
        return registry;
    }

    @Nonnull
    public Class<? super T> getTypeClass() {
        return typeClass;
    }

    public int getIndex() {
        return index;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return "CastComponentType[" + typeClass.getSimpleName() + "@" + index + "]";
    }
}
