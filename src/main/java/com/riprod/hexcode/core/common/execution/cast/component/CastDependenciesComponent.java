package com.riprod.hexcode.core.common.execution.cast.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.ecs.LifecycleCastComponent;
import com.riprod.hexcode.utils.HexRefs;

public final class CastDependenciesComponent implements LifecycleCastComponent {

    private List<Ref<EntityStore>> dependents = new ArrayList<>();

    public CastDependenciesComponent() {
    }

    public void add(@Nonnull Ref<EntityStore> dependent) {
        dependents.add(dependent);
    }

    @Override
    public void onCastEnd(@Nonnull HexCast cast, @Nonnull CommandBuffer<EntityStore> buffer) {
        for (var dependent : dependents) {
            var live = HexRefs.live(dependent, buffer);
            if (live != null) {
                buffer.tryRemoveEntity(live, RemoveReason.REMOVE);
            }
        }
        dependents.clear();
    }

    @Nonnull
    @Override
    public CastDependenciesComponent copy() {
        var copy = new CastDependenciesComponent();
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
