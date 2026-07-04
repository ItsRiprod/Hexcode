package com.riprod.hexcode.core.common.execution.precast;

import java.util.Set;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;

public class CastDecaySystem extends WorldEventSystem<EntityStore, HexCastEvent.Pre> {

    private final Set<Dependency<EntityStore>> dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, CastChargesSystem.class));

    public CastDecaySystem() {
        super(HexCastEvent.Pre.class);
    }

    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return dependencies;
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexCastEvent.Pre event) {
        if (event.isCancelled()) return;
        HexContext context = event.getContext();
        if (!context.isApplyVolatilityDecay()) return;
        if (context.getCastSlotKey() != null) return;
        if (!(context.getHexRoot() instanceof PlayerHexRoot playerRoot)) return;
        Ref<EntityStore> casterRef = playerRoot.getSourceRef(buffer);
        if (casterRef == null || !casterRef.isValid()) return;
        HexStats tracker = context.getHexStats();
        if (tracker == null) return;

        ExecutionComponent exec = buffer.ensureAndGetComponent(casterRef,
                ExecutionComponent.getComponentType());

        float stability = Math.max(0f, Math.min(100f, playerRoot.resolveStability(buffer)));
        float retention = (float) Math.pow(stability / 100f, exec.getCastCount());
        float volMax = tracker.getInitialVolatility() + playerRoot.resolveVolatility(buffer);
        float startingBudget = Math.max(0f, volMax * retention);
        tracker.setVolatility(startingBudget);
        tracker.setInitialVolatility(startingBudget);
        exec.advanceCast();
    }
}
