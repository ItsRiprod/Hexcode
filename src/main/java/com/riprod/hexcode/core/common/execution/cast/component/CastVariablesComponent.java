package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public final class CastVariablesComponent implements CastComponent {

    private Map<String, HexVar> variables = new HashMap<>();

    public CastVariablesComponent() {
    }

    @Nullable
    public HexVar get(String key) {
        return variables.get(key);
    }

    public void put(String key, @Nullable HexVar value) {
        variables.put(key, value == null ? null : value.copy());
    }

    @Nonnull
    public Map<String, HexVar> all() {
        return variables;
    }

    @Nonnull
    @Override
    public CastVariablesComponent copy() {
        CastVariablesComponent copy = new CastVariablesComponent();
        copy.variables = new HashMap<>(this.variables);
        return copy;
    }

    @Nonnull
    @Override
    public CastVariablesComponent branch() {
        return this;
    }

    public static final CastComponentType<CastVariablesComponent> TYPE = HexComponentRegistry.register(
            CastVariablesComponent.class, "Variables", a -> new CastVariablesComponent());
}
