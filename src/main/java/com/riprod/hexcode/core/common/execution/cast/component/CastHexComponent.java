package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class CastHexComponent implements CastComponent {

    @Nonnull
    private Hex hex;

    public CastHexComponent(@Nonnull Hex hex) {
        this.hex = hex;
    }

    @Nonnull
    public Hex getHex() {
        return hex;
    }

    public void setHex(@Nonnull Hex hex) {
        this.hex = hex;
    }

    @Nonnull
    @Override
    public CastHexComponent copy() {
        return new CastHexComponent(hex.clone());
    }

    @Nonnull
    @Override
    public CastHexComponent branch() {
        return this;
    }

    public static final CastComponentType<CastHexComponent> TYPE = HexComponentRegistry.register(
            CastHexComponent.class, "Hex");
}
