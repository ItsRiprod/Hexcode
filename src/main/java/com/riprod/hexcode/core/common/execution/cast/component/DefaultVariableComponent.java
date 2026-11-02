package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public final class DefaultVariableComponent implements CastComponent {

    @Nullable
    private HexVar value;

    public DefaultVariableComponent(@Nullable HexVar value) {
        this.value = value;
    }

    @Nullable
    public HexVar get() {
        return value;
    }

    public void set(@Nullable HexVar value) {
        this.value = value == null ? null : value.copy();
    }

    @Nonnull
    @Override
    public DefaultVariableComponent copy() {
        return new DefaultVariableComponent(value == null ? null : value.copy());
    }

    @Nonnull
    @Override
    public DefaultVariableComponent branch() {
        return new DefaultVariableComponent(value == null ? null : value.copy());
    }

    public static final CastComponentType<DefaultVariableComponent> TYPE = HexComponentRegistry.register(
            DefaultVariableComponent.class, "DefaultVariable",
            a -> {
                var origin = a.get(CastOriginComponent.TYPE);
                return origin != null ? new DefaultVariableComponent(origin.getSelfVariable()) : null;
            });
}
