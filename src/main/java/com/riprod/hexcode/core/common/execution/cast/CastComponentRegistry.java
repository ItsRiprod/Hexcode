package com.riprod.hexcode.core.common.execution.cast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public final class CastComponentRegistry {

    private final List<CastComponentType<?>> types = new ArrayList<>();

    @Nonnull
    public <T extends CastComponent> CastComponentType<T> registerComponent(
            @Nonnull Class<T> typeClass, @Nonnull Supplier<T> supplier) {
        CastComponentType<T> type = new CastComponentType<>(typeClass, supplier, types.size());
        types.add(type);
        return type;
    }

    public int size() {
        return types.size();
    }
}
