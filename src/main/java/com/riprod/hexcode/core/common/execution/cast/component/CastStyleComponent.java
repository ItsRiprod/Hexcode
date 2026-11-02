package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.hexes.registry.HexStyleAsset;

public final class CastStyleComponent implements CastComponent {

    @Nonnull
    private HexStyleAsset style;

    public CastStyleComponent(@Nonnull HexStyleAsset style) {
        this.style = style;
    }

    @Nonnull
    public HexStyleAsset get() {
        return style;
    }

    public void set(@Nonnull HexStyleAsset style) {
        this.style = style;
    }

    @Nonnull
    public HexStyleAsset mutable() {
        style = style.clone();
        return style;
    }

    @Nonnull
    @Override
    public CastStyleComponent copy() {
        return new CastStyleComponent(style.clone());
    }

    @Nonnull
    @Override
    public CastStyleComponent branch() {
        return new CastStyleComponent(style);
    }

    public static final CastComponentType<CastStyleComponent> TYPE = HexComponentRegistry.register(
            CastStyleComponent.class, "Style", a -> new CastStyleComponent(HexStyleAsset.empty()));
}
