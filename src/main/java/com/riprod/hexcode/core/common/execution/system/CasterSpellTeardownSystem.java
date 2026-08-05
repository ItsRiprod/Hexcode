package com.riprod.hexcode.core.common.execution.system;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.system.HexConstructTeardownSystem;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;
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

            // trackers are the same VolatilityComponent instances the caster's remote constructs hold, so
            // zeroing here makes each one abort through its own handler on its next tick
            casterState.cancelAll(ref);

            for (Ref<EntityStore> dependent : casterState.getDependencyList()) {
                Ref<EntityStore> live = HexRefs.live(dependent, store);
                if (live == null) {
                    continue;
                }
                buffer.tryRemoveEntity(live, RemoveReason.REMOVE);
            }
            casterState.getDependencies().clear();

            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            purgeQueuedGlyphs(store, ref, uuidComponent != null ? uuidComponent.getUuid() : null);
        } catch (Exception e) {
            LOGGER.atSevere().log("CasterSpellTeardownSystem.onEntityRemove failed: %s", e.getMessage());
        }
    }

    private void purgeQueuedGlyphs(Store<EntityStore> store, Ref<EntityStore> ref, @Nullable UUID uuid) {
        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        if (queue == null) {
            return;
        }
        int purged = queue.removeIf(item -> {
            HexRoot root = item.ctx().getHexRoot();
            if (root == null) {
                return false;
            }
            if (uuid != null && root instanceof PlayerHexRoot playerRoot) {
                PersistentRef entity = playerRoot.getEntity();
                return entity != null && uuid.equals(entity.getUuid());
            }
            return root.getSourceRef(store) == ref;
        });
        if (purged > 0) {
            LOGGER.atFine().log("dropped %d queued glyph(s) for a departing caster", purged);
        }
    }
}
