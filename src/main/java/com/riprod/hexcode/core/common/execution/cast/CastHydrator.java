package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;

@FunctionalInterface
public interface CastHydrator<T extends CastComponent> {

    @Nullable
    T hydrate(@Nonnull CastAssembly assembly);
}
