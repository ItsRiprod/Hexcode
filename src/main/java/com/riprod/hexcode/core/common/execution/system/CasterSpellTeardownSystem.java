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
import com.riprod.hexcode.core.common.construct.system.HexConstructTeardownSystem;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;

public class CasterSpellTeardownSystem extends RefSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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

            // trackers are the same HexStats instances the caster's remote constructs hold, so
            // zeroing here makes each one abort through its own handler on its next tick
            casterState.cancelAll(ref);

            for (Ref<EntityStore> dependent : casterState.getDependencyList()) {
                if (dependent == null) {
                    continue;
                }
                buffer.tryRemoveEntity(dependent, RemoveReason.REMOVE);
            }
            casterState.getDependencies().clear();

            purgeQueuedGlyphs(store, ref);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] CasterSpellTeardownSystem.onEntityRemove failed: %s", e.getMessage());
        }
    }

    private void purgeQueuedGlyphs(Store<EntityStore> store, Ref<EntityStore> ref) {
        HexExecutionQueue queue = store.getResource(HexExecutionQueue.getResourceType());
        if (queue == null) {
            return;
        }
        int purged = queue.removeIf(item -> {
            HexRoot root = item.ctx().getHexRoot();
            return root != null && root.getSourceRef(store) == ref;
        });
        if (purged > 0) {
            LOGGER.atInfo().log("[hexcode] dropped %d queued glyph(s) for a departing caster", purged);
        }
    }
}
