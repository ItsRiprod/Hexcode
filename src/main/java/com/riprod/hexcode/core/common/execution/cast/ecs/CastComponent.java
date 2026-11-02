package com.riprod.hexcode.core.common.execution.cast.ecs;

import javax.annotation.Nonnull;

public interface CastComponent {

    @Nonnull
    CastComponent copy();

    @Nonnull
    CastComponent branch();
}
