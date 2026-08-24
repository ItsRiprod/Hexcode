package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;

public interface CastOverlay<T extends CastComponent> {

    void applyTo(@Nonnull T target);
}
