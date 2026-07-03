package com.riprod.hexcode.core.common.pedestal.events;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.registry.ImbuementProfileRegistry;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskSystem;
import com.riprod.hexcode.core.common.obelisk.utils.ObeliskBlockUtil;

import io.sentry.util.Pair;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.utils.HexSlot;

public class PedestalSystem {

    public static final HytaleLogger logger = HytaleLogger.forEnclosingClass();
    public static final float PREVIEW_RADIUS = 3.5f;
    public static final Vector3f ACTIVE_HEX_OFFSET = new Vector3f(0, 1.3f, 0);
    public static final Vector3f HEX_SLOT_OFFSET = new Vector3f(0, -0.8f, 0);

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

    public static void registerObelisks(CommandBuffer<EntityStore> buffer, World world,
            PedestalBlockComponent pedestal) {
        List<Pair<Vector3i, ObeliskBlockComponent>> obeliskPairs = ObeliskBlockUtil
                .getAvailableObelisks(pedestal.getLocation(), pedestal.getObeliskRange(),
                        world, pedestal.getMaxObelisks());
        List<Vector3i> obelisks = new ArrayList<>();
        for (Pair<Vector3i, ObeliskBlockComponent> obeliskPair : obeliskPairs) {
            obelisks.add(obeliskPair.getFirst());
            obeliskPair.getSecond().setRegisteredPedestalLoc(pedestal.getLocation());
        }
        List<Vector3i> removedObelisks = pedestal.setActiveObelisks(obelisks);
        ObeliskSystem.cleanupObelisks(buffer, world, removedObelisks);
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
