package com.riprod.hexcode.core.common.pedestal.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementProfileRegistry;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskSystem;
import com.riprod.hexcode.core.common.obelisk.utils.ObeliskBlockUtil;

import io.sentry.util.Pair;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.constants.PedestalState;
import com.riprod.hexcode.core.state.crafting.entity.AnchorEntity;
import com.riprod.hexcode.core.state.crafting.entity.PedestalEntity;
import com.riprod.hexcode.core.state.crafting.handlers.CraftingDragHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.container.ContainerNodeHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.SlotNodeHandler;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.core.state.crafting.utils.RadialPositionUtil;
import com.riprod.hexcode.utils.HexSlot;

public class PedestalSystem {

    public static final HytaleLogger logger = HytaleLogger.forEnclosingClass();
    public static final float PREVIEW_RADIUS = 3.5f;
    public static final Vector3f ACTIVE_HEX_OFFSET = new Vector3f(0, 1.3f, 0);
    public static final Vector3f HEX_SLOT_OFFSET = new Vector3f(0, -0.8f, 0);

    public static void SpawnHexPreviews(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            PedestalBlockComponent pedestal,
            HexcodeSessionComponent session) {

        ImbuementProfileAsset profile = session.getProfile();
        if (profile == null) return;
        Map<String, SlotAsset> slots = profile.getSlots();
        if (slots.isEmpty()) return;

        Ref<EntityStore> anchorRef = session.getAnchorRef();
        if (anchorRef == null || !anchorRef.isValid()) {
            return;
        }

        Vector3d anchorPos = PedestalEntity.getAnchorPosition(pedestal.getLocation());
        List<Vector3f> offsets = RadialPositionUtil.calculateOffsets(slots.size(), PREVIEW_RADIUS, 0,
                HEX_SLOT_OFFSET);
        List<Ref<EntityStore>> spawnedRefs = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, SlotAsset> entry : slots.entrySet()) {
            Vector3f offset = offsets.get(i++);
            String slotKey = entry.getKey();
            SlotAsset slotAsset = entry.getValue();
            Hex hex = session.getHexAt(slotKey, buffer);
            Ref<EntityStore> hexRef = ContainerNodeHandler.INSTANCE.spawnContainer(buffer, hex, anchorRef,
                    anchorPos, offset, playerRef, slotAsset);
            if (hexRef != null) {
                buffer.addComponent(hexRef, SlotComponent.getComponentType(),
                        new SlotComponent(slotKey));
            }
            spawnedRefs.add(hexRef);
        }

        session.setHexPreviewRefs(spawnedRefs);
    }

    public static void enterCrafting(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            PedestalBlockComponent pedestal, Ref<EntityStore> selectedAnchorNodeRef) {

        HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
        if (session == null)
            return;

        List<Ref<EntityStore>> refs = session.getHexPreviewRefs();
        if (refs == null || refs.isEmpty()) {
            return;
        }

        for (Ref<EntityStore> ref : refs) {
            if (ref == null || !ref.isValid() || ref.equals(selectedAnchorNodeRef)) {
                continue;
            }

            HexComponent hexComp = buffer.getComponent(ref, HexComponent.getComponentType());
            if (hexComp != null) {
                Map<String, Ref<EntityStore>> childRefs = hexComp.getChildGlyphRefs();
                if (childRefs != null) {
                    for (Ref<EntityStore> glyphRef : childRefs.values()) {
                        if (glyphRef == null || !glyphRef.isValid())
                            continue;
                        SlotNodeHandler.INSTANCE.despawnSlotsForGlyph(buffer, glyphRef);
                        buffer.tryRemoveComponent(glyphRef, MountedComponent.getComponentType());
                        buffer.tryRemoveEntity(glyphRef, RemoveReason.REMOVE);
                    }
                }
            }

            if (ref.isValid()) {
                buffer.tryRemoveComponent(ref, MountedComponent.getComponentType());
                buffer.tryRemoveEntity(ref, RemoveReason.REMOVE);
            }
        }

        Vector3d anchorPos = PedestalEntity.getAnchorPosition(session.getPedestalLocation());
        Vector3d activePos = new Vector3d(
                anchorPos.x + ACTIVE_HEX_OFFSET.x,
                anchorPos.y + ACTIVE_HEX_OFFSET.y,
                anchorPos.z + ACTIVE_HEX_OFFSET.z);
        TransformComponent anchorTransform = buffer.getComponent(selectedAnchorNodeRef,
                TransformComponent.getComponentType());
        anchorTransform.getPosition().set(activePos);
        anchorTransform.getRotation().set(0f, 0f, 0f);
        if (buffer.getComponent(selectedAnchorNodeRef, MountedComponent.getComponentType()) != null) {
            buffer.removeComponent(selectedAnchorNodeRef, MountedComponent.getComponentType());
        }

        session.setHexPreviewRefs(List.of(selectedAnchorNodeRef));

        World world = buffer.getExternalData().getWorld();
        updateState(buffer, pedestal, session, world, PedestalState.CRAFTING);
    }

    public static void saveHexToBook(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            HexcodeSessionComponent session) {

        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            return;
        }

        List<Ref<EntityStore>> previewRefs = session.getHexPreviewRefs();
        if (previewRefs == null || previewRefs.isEmpty()) {
            return;
        }

        Ref<EntityStore> activeHexRef = previewRefs.get(0);
        if (activeHexRef == null || !activeHexRef.isValid()) {
            return;
        }

        HexComponent hexComp = buffer.getComponent(activeHexRef, HexComponent.getComponentType());
        if (hexComp == null) {
            return;
        }

        Hex hex = hexComp.getHex().clone();
        HexUtils.compress(hex);

        ItemStack stack = session.getStoredItem();
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (hex.getGlyphs().isEmpty()) {
            stack = ImbuementUtils.clear(stack, slotKey);
        } else {
            stack = ImbuementUtils.write(stack, slotKey, ImbuementUtils.fromHex(hex));
        }
        session.setStoredItem(stack);
    }

    public static void saveHexToBook(Store<EntityStore> store, Ref<EntityStore> playerRef,
            HexcodeSessionComponent session) {

        String slotKey = session.getActiveSlotKey();
        if (slotKey == null)
            return;

        List<Ref<EntityStore>> previewRefs = session.getHexPreviewRefs();
        if (previewRefs == null || previewRefs.isEmpty())
            return;

        Ref<EntityStore> activeHexRef = previewRefs.get(0);
        if (activeHexRef == null || !activeHexRef.isValid())
            return;

        HexComponent hexComp = store.getComponent(activeHexRef, HexComponent.getComponentType());
        if (hexComp == null)
            return;

        Hex hex = hexComp.getHex().clone();
        HexUtils.compress(hex);

        ItemStack stack = session.getStoredItem();
        if (stack == null || stack.isEmpty())
            return;

        if (hex.getGlyphs().isEmpty()) {
            stack = ImbuementUtils.clear(stack, slotKey);
        } else {
            stack = ImbuementUtils.write(stack, slotKey, ImbuementUtils.fromHex(hex));
        }
        session.setStoredItem(stack);
    }

    public static void exitCrafting(CommandBuffer<EntityStore> accessor, Ref<EntityStore> playerRef,
            PedestalBlockComponent pedestal, HexcodeSessionComponent session) {

        ObeliskDispatcher.dispatchExitCrafting(accessor, pedestal, playerRef);
        saveHexToBook(accessor, playerRef, session);

        SlotNodeHandler.INSTANCE.despawn(accessor, session);
        session.setSlotNodeRefs(new ArrayList<>());

        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            CraftingDragHandler.endDrag(accessor, craftingComp.getDraggingRef(),
                    craftingComp.getHeadAnchorRef(), craftingComp);
        }

        AnchorEntity.DespawnHexPreviews(accessor, session);

        Ref<EntityStore> anchorNodeRef = session.getAnchorNodeRef();
        if (anchorNodeRef != null && anchorNodeRef.isValid()) {
            accessor.tryRemoveEntity(anchorNodeRef, RemoveReason.REMOVE);
            session.setAnchorNodeRef(null);
        }

        session.setActiveSlotKey(null);

        if (craftingComp != null) {
            craftingComp.clearCraftingState();
        }
    }

    public static void handleItemPlacement(CommandBuffer<EntityStore> buffer,
            Player player, ItemStack stack, HexSlot slot, PedestalBlockComponent pedestalComponent,
            HexcodeSessionComponent session, Vector3i blockPos) {

        ImbuementProfileAsset profile = ImbuementProfileRegistry.first(stack);
        if (profile == null) return;

        Vector3d anchorPos = PedestalEntity.getAnchorPosition(blockPos);

        // pedestal stores exactly one of the item; the rest stays in the player's inventory
        ItemStack singleStack = stack.withQuantity(1);
        session.setStoredItem(singleStack);
        session.setSourceSlot(slot);
        session.setProfileId(profile.getId());

        pedestalComponent.setBookAssetId(stack.getItem().getId());

        Ref<EntityStore> oldDisplay = session.getImbuedItemDisplayRef();
        if (oldDisplay != null && oldDisplay.isValid()) {
            buffer.removeEntity(oldDisplay, RemoveReason.REMOVE);
        }

        Ref<EntityStore> newDisplayRef = PedestalEntity.spawnBookDisplay(
                buffer, pedestalComponent, session, anchorPos, singleStack, player.getReference());
        session.setImbuedItemDisplayRef(newDisplayRef);
        PlayerUtils.consumeOneFromHand(buffer, player.getReference(), slot);
    }

    public static void enterSelecting(PedestalBlockComponent pedestalComponent, Player player,
            World world, CommandBuffer<EntityStore> buffer) {

        Ref<EntityStore> sessionRef = SessionUtils.getSessionRef(pedestalComponent);
        HexcodeSessionComponent session = sessionRef != null
                ? buffer.getComponent(sessionRef, HexcodeSessionComponent.getComponentType())
                : null;

        if (session == null) {
            return;
        }

        PedestalSystem.SpawnHexPreviews(buffer, player.getReference(), pedestalComponent, session);

        HexcasterCraftingComponent craftingComp = buffer.ensureAndGetComponent(player.getReference(),
                HexcasterCraftingComponent.getComponentType());
        craftingComp.setSessionRef(sessionRef);

        List<Pair<Vector3i, ObeliskBlockComponent>> obeliskPairs = ObeliskBlockUtil
                .getAvailableObelisks(pedestalComponent.getLocation(), pedestalComponent.getObeliskRange(),
                        world, pedestalComponent.getMaxObelisks());
        List<Vector3i> obelisks = new ArrayList<>();
        for (Pair<Vector3i, ObeliskBlockComponent> obeliskPair : obeliskPairs) {
            obelisks.add(obeliskPair.getFirst());
            obeliskPair.getSecond().setRegisteredPedestalLoc(pedestalComponent.getLocation());
        }
        List<Vector3i> removedObelisks = pedestalComponent.setActiveObelisks(obelisks);
        ObeliskSystem.cleanupObelisks(buffer, world, removedObelisks);

        ImbuementProfileAsset profile = session.getProfile();
        if (profile != null && profile.isSkipSelecting() && !profile.getSlots().isEmpty()) {
            // single-slot: stash the only key and let SessionTickSystem enter crafting next tick
            // (the just-spawned preview Refs are not yet valid this tick)
            String onlyKey = profile.getSlots().keySet().iterator().next();
            session.setPendingReenterSlotKey(onlyKey);
        }

        updateState(buffer, pedestalComponent, session, world, PedestalState.SELECTING);
        HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                .dispatch(CraftingEvent.builder(CraftingEvent.Reason.ENTERED_SELECTING, player.getReference())
                        .pedestal(pedestalComponent)
                        .build());
    }

    public static void handleReady(CommandBuffer<EntityStore> accessor, HexcodeSessionComponent session,
            PedestalBlockComponent pedestal,
            World world) {

        ItemStack stored = session.getStoredItem();
        if (stored == null || stored.isEmpty()) {
            return;
        }

        updateState(accessor, pedestal, session, world, PedestalState.READY);
    }

    public static void updateState(CommandBuffer<EntityStore> accessor, PedestalBlockComponent pedestal,
            HexcodeSessionComponent session, World world,
            PedestalState state) {

        PedestalState previousState = session.getState();

        Vector3i blockPos = pedestal.getLocation();

        String defaultName = switch (state) {
            case IDLE -> "Idle";
            case READY -> "Ready";
            case SELECTING -> "Selecting";
            case CRAFTING -> "Crafting";
        };
        ImbuementProfileAsset profile = session.getProfile();
        String animName = profile != null
                ? profile.getStateAnimations().getOrDefault(state, defaultName)
                : defaultName;

        boolean canSwitch = canSwitchState(accessor, pedestal, state);

        session.setState(state);

        Ref<EntityStore> displayRef = session.getImbuedItemDisplayRef();
        if (displayRef != null && displayRef.isValid()) {
            AnimationUtils.playAnimation(displayRef, AnimationSlot.Action, animName, accessor);
        }

        if (canSwitch) {
            PedestalBlockUtil.changeBlockState(world, blockPos, defaultName);
            ObeliskSystem.updateState(accessor, pedestal, world, previousState, state);
        }
    }

    private static boolean canSwitchState(CommandBuffer<EntityStore> buffer,
            PedestalBlockComponent pedestal, PedestalState newState) {
        return true;
    }
}
