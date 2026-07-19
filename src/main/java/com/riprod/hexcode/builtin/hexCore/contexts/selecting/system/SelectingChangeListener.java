package com.riprod.hexcode.builtin.hexCore.contexts.selecting.system;

import javax.annotation.Nonnull;

import java.util.Map;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.HexContextChangeEvent;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.GravityUtil;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.utils.SelectingScene;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class SelectingChangeListener extends WorldEventSystem<EntityStore, HexContextChangeEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public SelectingChangeListener() {
        super(HexContextChangeEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContextChangeEvent event) {
        Ref<EntityStore> player = event.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }

        if (SelectingState.CONTEXT_ID.equals(event.getNewContextId())) {
            enter(buffer, player);
            return;
        }

        SelectingState state = buffer.getComponent(player, SelectingState.getComponentType());
        if (state == null) {
            return;
        }
        buffer.tryRemoveComponent(player, SelectingState.getComponentType());

        if (CraftingState.CONTEXT_ID.equals(event.getNewContextId())) {
            teardownPreviewsIfOwner(buffer, player);
            return;
        }
        ContextTransitionService.setInContextStat(buffer, player, false);
        GravityUtil.exitFly(buffer, player);
        endSessionIfOwner(buffer, player);
    }

    private static void enter(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        buffer.ensureComponent(player, HexcasterCraftingComponent.getComponentType());
        buffer.putComponent(player, SelectingState.getComponentType(), new SelectingState());
        ContextTransitionService.setInContextStat(buffer, player, true);
        GravityUtil.enterFly(buffer, player);

        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        HexcodeSessionComponent session = pedestal != null
                ? SessionUtils.resolveSession(pedestal, buffer)
                : null;
        if (pedestal == null || session == null || !session.isOwner(player)) {
            return;
        }

        World world = buffer.getExternalData().getWorld();
        SelectingScene.spawnPreviews(buffer, player, pedestal, session);
        PedestalSystem.registerObelisks(buffer, world, pedestal);

        ImbuementProfileAsset profile = session.getProfile();
        if (profile != null) {
            Map<String, PedestalSlot> slots = profile.resolveSlots(session.getStoredItem());
            if (profile.isSkipSelecting(session.getStoredItem()) && !slots.isEmpty()) {
                session.setPendingReenterSlotKey(slots.keySet().iterator().next());
            }
        }

        PedestalSystem.updateState(buffer, pedestal, session, world, PedestalState.SELECTING);
        HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                .dispatch(CraftingEvent.builder(CraftingEvent.Reason.ENTERED_SELECTING, player)
                        .pedestal(pedestal)
                        .build());
    }

    private static void teardownPreviewsIfOwner(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        Ref<EntityStore> sessionRef = SessionUtils.getSessionRefByPlayer(player, buffer);
        HexcodeSessionComponent session = sessionRef != null
                ? buffer.getComponent(sessionRef, HexcodeSessionComponent.getComponentType())
                : null;
        if (session != null && session.isOwner(player)) {
            SessionUtils.despawnPreviewScene(buffer, session);
        }
    }

    private static void endSessionIfOwner(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        Ref<EntityStore> sessionRef = SessionUtils.getSessionRefByPlayer(player, buffer);
        if (sessionRef == null) {
            return;
        }
        HexcodeSessionComponent session = buffer.getComponent(sessionRef,
                HexcodeSessionComponent.getComponentType());
        if (session == null) {
            return;
        }
        World world = buffer.getExternalData().getWorld();
        if (session.isOwner(player)) {
            SessionUtils.endSession(buffer, sessionRef, world);
        } else {
            session.removeParticipant(player);
            HexcasterCraftingComponent craftingComp = buffer.getComponent(player,
                    HexcasterCraftingComponent.getComponentType());
            if (craftingComp != null) {
                craftingComp.clear(buffer);
            }
        }
    }
}
