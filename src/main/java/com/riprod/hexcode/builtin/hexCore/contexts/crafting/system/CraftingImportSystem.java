package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;

public class CraftingImportSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
            if (pedestal == null) {
                return;
            }
            HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
            if (session == null || session.getPendingImportHex() == null) {
                return;
            }

            String savedKey = session.getActiveSlotKey();
            Hex importedHex = session.getPendingImportHex();
            session.setPendingImportHex(null);

            // the import replaces the slot content; null the active slot so the crafting
            // teardown save cannot overwrite the imported hex with the discarded scene
            session.setActiveSlotKey(null);
            if (savedKey != null) {
                ItemStack stack = session.getStoredItem();
                stack = ImbuementUtils.write(stack, savedKey, ImbuementUtils.fromHex(importedHex));
                session.setStoredItem(stack);
            }
            session.setPendingReenterSlotKey(savedKey);

            ContextTransitionService.transitionFrom(buffer, player,
                    CraftingState.CONTEXT_ID, SelectingState.CONTEXT_ID, SelectingState.PRIORITY);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] crafting import failed");
        }
    }
}
