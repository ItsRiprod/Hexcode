package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public final class BranchVariablesComponent implements CastComponent {

    @Nullable
    private Map<String, HexVar> local;

    public BranchVariablesComponent() {
    }

    public void enterScope() {
        this.local = this.local == null
                ? new HashMap<>(4)
                : new HashMap<>(this.local);
    }

    @Nullable
    public HexVar get(String key) {
        return local != null ? local.get(key) : null;
    }

    public void put(String key, @Nullable HexVar value) {
        if (local != null) local.put(key, value);
    }

    @Nonnull
    @Override
    public BranchVariablesComponent copy() {
        var copy = new BranchVariablesComponent();
        copy.local = local != null ? new HashMap<>(local) : null;
        return copy;
    }

    @Nonnull
    @Override
    public BranchVariablesComponent branch() {
        var branch = new BranchVariablesComponent();
        branch.local = local;
        return branch;
    }

    public static final CastComponentType<BranchVariablesComponent> TYPE = HexComponentRegistry.register(
            BranchVariablesComponent.class, "BranchVariables", a -> new BranchVariablesComponent());
}
