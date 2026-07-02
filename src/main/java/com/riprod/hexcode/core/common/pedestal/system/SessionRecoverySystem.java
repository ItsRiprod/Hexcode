package com.riprod.hexcode.core.common.pedestal.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.utils.PedestalItemUtil;
import com.riprod.hexcode.utils.HexSlot;

public class SessionRecoverySystem extends RefSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return HexcodeSessionComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        if (reason != AddReason.LOAD) {
            return;
        }
        try {
            HexcodeSessionComponent session = store.getComponent(ref, HexcodeSessionComponent.getComponentType());
            if (session == null) {
                return;
            }

            LOGGER.atInfo().log("[hexcode] recovering orphaned session for reconnecting player");
            ItemStack item = session.getStoredItem();
            if (item != null && !item.isEmpty()) {
                HexSlot slot = session.getSourceSlot();
                if (slot == null) {
                    slot = HexSlot.MainHand;
                }
                PedestalItemUtil.returnBookToPlayer(buffer, ref, item, slot);
            }
            buffer.tryRemoveComponent(ref, HexcodeSessionComponent.getComponentType());
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] session recovery failed");
        }
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
    }
}
