package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class CastDependenciesComponent implements CastComponent {

    private List<Ref<EntityStore>> dependents = new ArrayList<>();

    public CastDependenciesComponent() {
    }

    public void add(@Nonnull Ref<EntityStore> dependent) {
        dependents.add(dependent);
    }

    @Nonnull
    public List<Ref<EntityStore>> getDependents() {
        return dependents;
    }

    public void clear() {
        dependents.clear();
    }

    @Nonnull
    @Override
    public CastDependenciesComponent copy() {
        CastDependenciesComponent copy = new CastDependenciesComponent();
        copy.dependents = new ArrayList<>(this.dependents);
        return copy;
    }

    @Nonnull
    @Override
    public CastDependenciesComponent branch() {
        return this;
    }

    public static final CastComponentType<CastDependenciesComponent> TYPE = HexComponentRegistry.register(
            CastDependenciesComponent.class, "Dependencies", a -> new CastDependenciesComponent());
}
