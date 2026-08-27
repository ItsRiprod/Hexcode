package com.riprod.hexcode.core.common.execution.system;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import com.riprod.hexcode.core.common.construct.system.HexConstructTeardownSystem;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.component.CastDependenciesComponent;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue;
import com.riprod.hexcode.utils.HexRefs;
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
            CasterStateComponent casterState = store.getComponent(ref, CasterStateComponent.getComponentType());
            if (casterState == null) {
                return;
            }

            HexCastStore casts = store.getResource(HexCastStore.getResourceType());

            // trackers are the same VolatilityComponent instances the caster's remote constructs hold, so
            // zeroing here makes each one abort through its own handler on its next tick
            casterState.cancelAll(casts);

            for (UUID castId : casterState.getActiveCastIds()) {
                HexCast cast = casts.get(castId);
                if (cast == null) {
                    continue;
                }
                CastDependenciesComponent dependencies =
                        cast.get(CastDependenciesComponent.getComponentType());
                if (dependencies != null) {
                    for (Ref<EntityStore> dependent : dependencies.getDependents()) {
                        Ref<EntityStore> live = HexRefs.live(dependent, store);
                        if (live == null) {
                            continue;
                        }
                        buffer.tryRemoveEntity(live, RemoveReason.REMOVE);
                    }
                    dependencies.clear();
                }
                casts.remove(castId);
            }

            purgeQueuedGlyphs(store, casterState.getActiveCastIds());
            casterState.getActiveCastIds().clear();
        } catch (Exception e) {
            LOGGER.atSevere().log("CasterSpellTeardownSystem.onEntityRemove failed: %s", e.getMessage());
        }
    }

    private void purgeQueuedGlyphs(Store<EntityStore> store, List<UUID> castIds) {
        if (castIds.isEmpty()) {
            return;
        }
        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        if (queue == null) {
            return;
        }
        Set<UUID> departing = new HashSet<>(castIds);
        int purged = queue.removeIf(item -> departing.contains(item.ctx().getExecutionId()));
        if (purged > 0) {
            LOGGER.atFine().log("dropped %d queued glyph(s) for a departing caster", purged);
        }
    }
}
