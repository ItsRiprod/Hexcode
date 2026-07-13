package com.riprod.hexcode.core.common.pedestal.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementProfileRegistry;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.utils.HexSlot;
import com.riprod.hexcode.utils.VfxUtil;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import io.sentry.util.Pair;

public class PedestalInteractionEvent {
    private static HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public static void HandleInteraction(CommandBuffer<EntityStore> accessor, Ref<EntityStore> playerRef,
            BlockPosition targetBlock) {
        try {
            handleInteractionInternal(accessor, playerRef, targetBlock);
        } catch (Exception e) {
            logger.atSevere().withCause(e).log("pedestal interaction failed at %s", targetBlock);
        }
    }

    private static void handleInteractionInternal(CommandBuffer<EntityStore> accessor, Ref<EntityStore> playerRef,
            BlockPosition targetBlock) {

        Vector3i blockPos = new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z);
        World world = accessor.getExternalData().getWorld();

        Player player = accessor.getComponent(playerRef, Player.getComponentType());
        if (player == null)
            return;

        PedestalBlockComponent pedestalComponent = BlockModule.getComponent(
                PedestalBlockComponent.getComponentType(), world,
                blockPos.x, blockPos.y, blockPos.z);
        if (pedestalComponent == null) {
            logger.atWarning().log("pedestal interaction failed: no PedestalBlockComponent at %s", blockPos);
            return;
        }

        Ref<EntityStore> existingSessionRef = SessionUtils.getSessionRef(pedestalComponent);
        HexcodeSessionComponent session = existingSessionRef != null
                ? accessor.getComponent(existingSessionRef, HexcodeSessionComponent.getComponentType())
                : null;

        Ref<EntityStore> playerSessionRef = SessionUtils.getSessionRefByPlayer(playerRef, accessor);

        if (playerSessionRef != null && !playerSessionRef.equals(existingSessionRef)) {
            PlayerRef pr = accessor.getComponent(playerRef, PlayerRef.getComponentType());
            if (pr != null) {
                pr.sendMessage(Message.raw("You are already in another crafting session."));
            }
            HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                    .dispatch(CraftingEvent.builder(CraftingEvent.Reason.DENIED_PEDESTAL_BUSY, playerRef)
                            .pedestal(pedestalComponent)
                            .message("You are already in another crafting session.")
                            .build());
            return;
        }

        if (session != null) {
            boolean isOwner = session.isOwner(playerRef);
            boolean isParticipant = session.isParticipant(playerRef);

            if (!isOwner && !isParticipant) {
                if (!session.isOpen()) {
                    PlayerRef pr = accessor.getComponent(playerRef, PlayerRef.getComponentType());
                    if (pr != null) {
                        pr.sendMessage(Message.raw("This pedestal is already in use!"));
                    }
                    HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                            .dispatch(CraftingEvent.builder(CraftingEvent.Reason.DENIED_PEDESTAL_BUSY, playerRef)
                                    .pedestal(pedestalComponent)
                                    .message("This pedestal is already in use!")
                                    .build());
                    return;
                }

                PedestalState state = session.getState();
                if (state == PedestalState.CRAFTING || state == PedestalState.SELECTING) {
                    joinAsCollaborator(accessor, playerRef, existingSessionRef, session);
                    return;
                }

                PlayerRef pr = accessor.getComponent(playerRef, PlayerRef.getComponentType());
                if (pr != null) {
                    pr.sendMessage(Message.raw("Waiting for the owner to finish setting up the pedestal."));
                }
                return;
            }

            if (isParticipant && !isOwner) {
                PedestalState state = session.getState();
                if (state == PedestalState.CRAFTING || state == PedestalState.SELECTING) {
                    HexcasterComponent hexcaster = accessor.getComponent(playerRef,
                            HexcasterComponent.getComponentType());
                    if (hexcaster != null && hexcaster.getState() == HexState.CRAFTING) {
                        hexcaster.requestStateChange(HexState.IDLE);
                        session.removeParticipant(playerRef);
                        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                                HexcasterCraftingComponent.getComponentType());
                        if (craftingComp != null) {
                            craftingComp.clear(accessor);
                        }
                        return;
                    }
                    joinAsCollaborator(accessor, playerRef, existingSessionRef, session);
                    return;
                }
                return;
            }
        }

        Pair<ItemStack, ItemStack> held = PlayerUtils.getItemFromHands(accessor, playerRef);
        ItemStack mainHand = held.getFirst();
        ItemStack utilityHand = held.getSecond();

        // no existing session: only create one if the player is holding a profile-bearing item
        if (session == null) {
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

            session = SessionUtils.createSession(accessor, pedestalComponent, blockPos,
                    playerRef, !pedestalComponent.isPerPlayer());
            existingSessionRef = pedestalComponent.getSessionRef();

            PedestalSystem.handleItemPlacement(accessor, player, chosen, chosenSlot,
                    pedestalComponent, session, blockPos);
            VfxUtil.particle("Area_Pulse", new Vector3d(blockPos.x, blockPos.y, blockPos.z), accessor);
            PedestalSystem.handleReady(accessor, session, pedestalComponent, world);
            return;
        }

        ItemStack storedItem = session.getStoredItem();
        boolean hasStoredItem = storedItem != null && !storedItem.isEmpty();

        if (!hasStoredItem) {
            // session exists but holds nothing — clean it up
            SessionUtils.endSession(accessor, existingSessionRef, world);
            return;
        }

        PedestalState state = session.getState();
        ImbuementProfileAsset currentProfile = session.getProfile();
        boolean skipSelecting = currentProfile != null && currentProfile.isSkipSelecting();

        logger.atFine().log("pedestal: hasItem, state=%s skipSelecting=%s", state, skipSelecting);
        if (state == PedestalState.CRAFTING) {
            if (skipSelecting) {
                Vector3i locA = pedestalComponent.getLocation();
                VfxUtil.sound("SFX_Deployable_Totem_Heal_Despawn",
                        new Vector3d(locA.x, locA.y, locA.z), accessor);
                PedestalSystem.exitCrafting(accessor, playerRef, pedestalComponent, session);
                SessionUtils.endSession(accessor, existingSessionRef, world);
                return;
            }
            PedestalSystem.exitCrafting(accessor, playerRef, pedestalComponent, session);
            PedestalSystem.enterSelecting(pedestalComponent, player, world, accessor);
        } else if (state == PedestalState.SELECTING) {
            Vector3i locB = pedestalComponent.getLocation();
            VfxUtil.sound("SFX_Deployable_Totem_Heal_Despawn", new Vector3d(locB.x, locB.y, locB.z), accessor);
            SessionUtils.endSession(accessor, existingSessionRef, world);
        } else {
            Vector3i locC = pedestalComponent.getLocation();
            VfxUtil.sound("SFX_Arcane_Workbench_Open_Local", new Vector3d(locC.x, locC.y, locC.z), accessor);
            PedestalSystem.enterSelecting(pedestalComponent, player, world, accessor);
        }
    }

    private static void joinAsCollaborator(CommandBuffer<EntityStore> accessor, Ref<EntityStore> playerRef,
            Ref<EntityStore> sessionRef, HexcodeSessionComponent session) {

        SessionUtils.joinSession(accessor, sessionRef, playerRef);

        HexcasterComponent hexcaster = accessor.getComponent(playerRef, HexcasterComponent.getComponentType());
        if (hexcaster != null) {
            hexcaster.requestStateChange(HexState.CRAFTING);
        }
    }
}
