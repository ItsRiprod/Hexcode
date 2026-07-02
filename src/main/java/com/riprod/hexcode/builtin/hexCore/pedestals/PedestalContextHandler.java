package com.riprod.hexcode.builtin.hexCore.pedestals;

import java.util.function.Consumer;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementProfileRegistry;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalInteractEvent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.utils.HexSlot;
import com.riprod.hexcode.utils.VfxUtil;

import io.sentry.util.Pair;

public class PedestalContextHandler implements Consumer<PedestalInteractEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void accept(PedestalInteractEvent event) {
        try {
            handle(event);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("pedestal interaction failed at %s", event.getBlockPos());
        }
    }

    private static void handle(PedestalInteractEvent event) {
        CommandBuffer<EntityStore> buffer = event.getBuffer();
        Ref<EntityStore> playerRef = event.getPlayerRef();
        PedestalBlockComponent pedestal = event.getPedestal();
        Vector3i blockPos = event.getBlockPos();
        World world = buffer.getExternalData().getWorld();

        Player player = buffer.getComponent(playerRef, Player.getComponentType());
        if (player == null) {
            return;
        }

        Ref<EntityStore> existingSessionRef = SessionUtils.getSessionRef(pedestal);
        HexcodeSessionComponent session = existingSessionRef != null
                ? buffer.getComponent(existingSessionRef, HexcodeSessionComponent.getComponentType())
                : null;

        Ref<EntityStore> playerSessionRef = SessionUtils.getSessionRefByPlayer(playerRef, buffer);
        if (playerSessionRef != null && !playerSessionRef.equals(existingSessionRef)) {
            deny(buffer, playerRef, pedestal, "You are already in another crafting session.");
            return;
        }

        if (session != null && !session.isOwner(playerRef)) {
            handleNonOwner(buffer, playerRef, pedestal, existingSessionRef, session);
            return;
        }

        if (session == null) {
            createSessionFromHeldItem(buffer, playerRef, player, pedestal, blockPos, world);
            return;
        }

        ItemStack storedItem = session.getStoredItem();
        if (storedItem == null || storedItem.isEmpty()) {
            SessionUtils.endSession(buffer, existingSessionRef, world);
            return;
        }

        PedestalState state = session.getState();
        ImbuementProfileAsset profile = session.getProfile();
        boolean skipSelecting = profile != null && profile.isSkipSelecting();
        Vector3i loc = pedestal.getLocation();

        if (state == PedestalState.CRAFTING) {
            if (skipSelecting) {
                VfxUtil.sound("SFX_Deployable_Totem_Heal_Despawn",
                        new Vector3d(loc.x, loc.y, loc.z), buffer);
                ContextTransitionService.exit(buffer, playerRef, CraftingState.CONTEXT_ID);
                return;
            }
            // save before the transition so the selecting scene renders the latest hex
            SessionUtils.saveHexToBook(buffer, playerRef, session);
            ContextTransitionService.transitionFrom(buffer, playerRef,
                    CraftingState.CONTEXT_ID, SelectingState.CONTEXT_ID, SelectingState.PRIORITY);
        } else if (state == PedestalState.SELECTING) {
            VfxUtil.sound("SFX_Deployable_Totem_Heal_Despawn", new Vector3d(loc.x, loc.y, loc.z), buffer);
            ContextTransitionService.exit(buffer, playerRef, SelectingState.CONTEXT_ID);
        } else {
            VfxUtil.sound("SFX_Arcane_Workbench_Open_Local", new Vector3d(loc.x, loc.y, loc.z), buffer);
            HexcasterCraftingComponent craftingComp = buffer.ensureAndGetComponent(playerRef,
                    HexcasterCraftingComponent.getComponentType());
            craftingComp.setSessionRef(existingSessionRef);
            ContextTransitionService.attemptEnter(buffer, playerRef,
                    SelectingState.CONTEXT_ID, SelectingState.PRIORITY);
        }
    }

    private static void handleNonOwner(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            PedestalBlockComponent pedestal, Ref<EntityStore> sessionRef, HexcodeSessionComponent session) {

        boolean isParticipant = session.isParticipant(playerRef);
        PedestalState state = session.getState();
        boolean joinable = state == PedestalState.CRAFTING || state == PedestalState.SELECTING;

        if (!isParticipant) {
            if (!session.isOpen()) {
                deny(buffer, playerRef, pedestal, "This pedestal is already in use!");
                return;
            }
            if (joinable) {
                joinAsCollaborator(buffer, playerRef, sessionRef);
                return;
            }
            PlayerRef pr = buffer.getComponent(playerRef, PlayerRef.getComponentType());
            if (pr != null) {
                pr.sendMessage(Message.raw("Waiting for the owner to finish setting up the pedestal."));
            }
            return;
        }

        if (!joinable) {
            return;
        }

        CasterComponent caster = buffer.getComponent(playerRef, CasterComponent.getComponentType());
        String currentContext = caster != null ? caster.getCurrentContext() : null;
        if (CraftingState.CONTEXT_ID.equals(currentContext)
                || SelectingState.CONTEXT_ID.equals(currentContext)) {
            session.removeParticipant(playerRef);
            HexcasterCraftingComponent craftingComp = buffer.getComponent(playerRef,
                    HexcasterCraftingComponent.getComponentType());
            if (craftingComp != null) {
                craftingComp.clear(buffer);
            }
            ContextTransitionService.exit(buffer, playerRef, currentContext);
            return;
        }
        joinAsCollaborator(buffer, playerRef, sessionRef);
    }

    private static void joinAsCollaborator(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            Ref<EntityStore> sessionRef) {
        HexcasterCraftingComponent craftingComp = buffer.ensureAndGetComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        SessionUtils.joinSession(buffer, sessionRef, playerRef);
        craftingComp.setSessionRef(sessionRef);
        ContextTransitionService.attemptEnter(buffer, playerRef,
                CraftingState.CONTEXT_ID, CraftingState.PRIORITY);
    }

    private static void createSessionFromHeldItem(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            Player player, PedestalBlockComponent pedestal, Vector3i blockPos, World world) {

        Pair<ItemStack, ItemStack> held = PlayerUtils.getItemFromHands(buffer, playerRef);
        ItemStack mainHand = held.getFirst();
        ItemStack utilityHand = held.getSecond();

        ItemStack chosen = null;
        HexSlot chosenSlot = null;
        ImbuementProfileAsset profile = null;
        if (mainHand != null && !mainHand.isEmpty()) {
            profile = ImbuementProfileRegistry.first(mainHand);
            if (profile != null) {
                chosen = mainHand;
                chosenSlot = HexSlot.MainHand;
            }
        }
        if (profile == null && utilityHand != null && !utilityHand.isEmpty()) {
            profile = ImbuementProfileRegistry.first(utilityHand);
            if (profile != null) {
                chosen = utilityHand;
                chosenSlot = HexSlot.OffHand;
            }
        }
        if (profile == null) {
            return;
        }

        HexcodeSessionComponent session = SessionUtils.createSession(buffer, pedestal, blockPos,
                playerRef, !pedestal.isPerPlayer());
        PedestalSystem.handleItemPlacement(buffer, player, chosen, chosenSlot, pedestal, session, blockPos);
        VfxUtil.particle("Area_Pulse", new Vector3d(blockPos.x, blockPos.y, blockPos.z), buffer);
        PedestalSystem.handleReady(buffer, session, pedestal, world);
    }

    private static void deny(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            PedestalBlockComponent pedestal, String message) {
        PlayerRef pr = buffer.getComponent(playerRef, PlayerRef.getComponentType());
        if (pr != null) {
            pr.sendMessage(Message.raw(message));
        }
        HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                .dispatch(CraftingEvent.builder(CraftingEvent.Reason.DENIED_PEDESTAL_BUSY, playerRef)
                        .pedestal(pedestal)
                        .message(message)
                        .build());
    }
}
