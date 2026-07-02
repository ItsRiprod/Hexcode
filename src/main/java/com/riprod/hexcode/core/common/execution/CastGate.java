package com.riprod.hexcode.core.common.execution;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.component.HexStats;

public final class CastGate {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private CastGate() {
    }

    /**
     * 
     * @param buffer
     * @param context
     * @return
     */
    public static boolean admit(@Nonnull CommandBuffer<EntityStore> buffer, @Nonnull HexContext context) {
        if (!(context.getHexRoot() instanceof PlayerHexRoot playerRoot)) {
            return true;
        }
        Ref<EntityStore> casterRef = playerRoot.getSourceRef(buffer);
        if (casterRef == null || !casterRef.isValid()) return true;

        HexcasterIdleComponent idle = buffer.getComponent(casterRef, HexcasterIdleComponent.getComponentType());
        if (idle == null) return true;

        HexStats tracker = context.getHexStats();
        if (tracker == null) return true;

        String slotKey = context.getCastSlotKey();
        tracker.setSlotKey(slotKey);

        if (slotKey == null) {
            int max = (int) playerRoot.resolveMaxMagicCharges(buffer);
            if (max <= 0) {
                sendNoSlotsMessage(buffer, casterRef);
                return false;
            }
            while (idle.getActiveCount() >= max) {
                idle.evictOldest();
            }
            float volMax = tracker.getInitialVolatility() + playerRoot.resolveVolatility(buffer);
            float startingBudget = Math.max(0f, volMax - idle.getCumulativeDecay());
            tracker.setVolatility(startingBudget);
            tracker.setInitialVolatility(startingBudget);
            idle.advanceCast(context.getCastDecayRate(), volMax);
        } else {
            idle.fizzleSlot(slotKey);
        }

        idle.registerActiveTracker(tracker);
        return true;
    }

    private static void sendNoSlotsMessage(CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        try {
            PlayerRef pr = buffer.getComponent(casterRef, PlayerRef.getComponentType());
            if (pr != null) pr.sendMessage(Message.raw("no spell slots available"));
        } catch (Exception e) {
            LOGGER.atWarning().log("CastGate noSlots message failed: %s", e.getMessage());
        }
    }
}
