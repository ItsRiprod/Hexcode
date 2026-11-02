package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;

import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;

public interface CastOverlay<T extends CastComponent> {

    void applyTo(@Nonnull T target);
}
