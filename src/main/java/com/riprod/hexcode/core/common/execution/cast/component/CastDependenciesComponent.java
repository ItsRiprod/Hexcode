package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.CastComponentType;

public final class CastDependenciesComponent implements CastComponent {

    private static CastComponentType<CastDependenciesComponent> componentType;

    public static CastComponentType<CastDependenciesComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(CastComponentType<CastDependenciesComponent> type) {
        componentType = type;
    }

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
}
