package com.riprod.hexcode.core.common.execution.precast;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;

public class CastChargesSystem extends WorldEventSystem<EntityStore, HexCastEvent.Pre> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public CastChargesSystem() {
        super(HexCastEvent.Pre.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexCastEvent.Pre event) {
        if (event.isCancelled()) return;
        HexContext context = event.getContext();
        if (!(context.getHexRoot() instanceof PlayerHexRoot playerRoot)) return;
        Ref<EntityStore> casterRef = playerRoot.getSourceRef(buffer);
        if (casterRef == null || !casterRef.isValid()) return;
        HexStats tracker = context.getHexStats();
        if (tracker == null) return;

        CasterStateComponent casterState = buffer.ensureAndGetComponent(casterRef,
                CasterStateComponent.getComponentType());

        String slotKey = context.getCastSlotKey();
        tracker.setSlotKey(slotKey);

        if (slotKey == null) {
            if (context.isRequireMagicCharges()) {
                int max = (int) playerRoot.resolveMaxMagicCharges(buffer);
                if (max <= 0) {
                    sendNoSlotsMessage(buffer, casterRef);
                    event.setCancelled(true);
                    return;
                }
                while (casterState.getActiveCount() >= max) {
                    casterState.evictOldest();
                }
            }
        } else {
            casterState.fizzleSlot(slotKey);
        }

        casterState.registerActiveTracker(tracker);
    }

    private static void sendNoSlotsMessage(CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        try {
            PlayerRef pr = buffer.getComponent(casterRef, PlayerRef.getComponentType());
            if (pr != null) pr.sendMessage(Message.raw("no spell slots available"));
        } catch (Exception e) {
            LOGGER.atWarning().log("CastChargesSystem noSlots message failed: %s", e.getMessage());
        }
    }
}
