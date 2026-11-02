package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class CastSlotComponent implements CastComponent {

    private final String slotKey;

    public CastSlotComponent(String slotKey) {
        this.slotKey = slotKey;
    }

    public String get() {
        return slotKey;
    }

    @Nonnull
    @Override
    public CastSlotComponent copy() {
        return this;
    }

    @Nonnull
    @Override
    public CastSlotComponent branch() {
        return this;
    }

    public static final CastComponentType<CastSlotComponent> TYPE = HexComponentRegistry.register(
            CastSlotComponent.class, "CastSlot");
}
