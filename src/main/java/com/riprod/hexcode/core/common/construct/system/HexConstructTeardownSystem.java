package com.riprod.hexcode.core.common.construct.system;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;

public class HexConstructTeardownSystem extends RefSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final int MAX_TEARDOWN_PASSES = 8;

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return HexEffectsComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            HexConstructSpawner.clearPendingApply(store, ref);

            HexEffectsComponent effects = store.getComponent(ref, HexEffectsComponent.getComponentType());
            if (effects == null || effects.getEffects().isEmpty()) {
                return;
            }

            ConstructTickContext ctx = new ConstructTickContext(buffer, ref);

            int pass = 0;
            while (!effects.getEffects().isEmpty() && pass++ < MAX_TEARDOWN_PASSES) {
                for (UUID effectId : new ArrayList<>(effects.getEffects().keySet())) {
                    HexStatus<?> status = effects.getEffects().get(effectId);
                    effects.removeEffect(effectId);
                    if (status == null) {
                        continue;
                    }
                    HexConstructSystem.abort(ConstructRegistry.get(status.getHandlerId()), status, ctx);
                }
            }

            int stranded = effects.getEffects().size();
            if (stranded > 0) {
                effects.getEffects().clear();
                LOGGER.atWarning().log("construct teardown gave up with %d effect(s) still re-arming", stranded);
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("HexConstructTeardownSystem failed: %s", e);
        }
    }
}
