package com.riprod.hexcode.core.common.execution.system;

import java.util.Set;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CastEndEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.construct.system.HexConstructTeardownSystem;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;
import com.riprod.hexcode.utils.LogScopes;

public class CasterSpellTeardownSystem extends RefSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CAST);

    private final Set<Dependency<EntityStore>> dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, HexConstructTeardownSystem.class));

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return dependencies;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return CasterStateComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            var casterState = store.getComponent(ref, CasterStateComponent.getComponentType());
            if (casterState == null) {
                return;
            }

            var casts = store.getResource(HexCastStore.getResourceType());
            for (var castId : casterState.getActiveCastIds()) {
                var cast = casts.get(castId);
                if (cast != null) {
                    HexExecuter.endCast(cast, CastEndEvent.Reason.CASTER_REMOVED, buffer);
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("CasterSpellTeardownSystem.onEntityRemove failed: %s", e.getMessage());
        }
    }
}
