package com.riprod.hexcode.builtin.hexCore.obelisks.encryption;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.EncodingStroke;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.obelisk.interfaces.ObeliskInterface;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class EncryptionObelisk implements ObeliskInterface {

    public static final String HANDLER_ID = "encryption";

    @Override
    public boolean isUniversal() {
        return false;
    }

    public static final String LOCKED_BLOCK_STATE = "Encryption";

    @Override
    public void onStateChange(CommandBuffer<EntityStore> buffer, ObeliskBlockComponent obelisk,
            Vector3i obeliskPos, PedestalState previousState, PedestalState newState) {
        HexcodeSessionComponent session = resolveSession(buffer, obelisk);
        if (session == null) return;
        EncryptionSessionState state = session.obeliskState(HANDLER_ID, EncryptionSessionState::new);
        if (newState == PedestalState.CRAFTING) {
            EncodingDisplay.refresh(buffer, state, obeliskPos, liveEncoding(buffer, session));
        } else {
            EncodingDisplay.despawn(buffer, state);
        }
        if (newState == PedestalState.SELECTING && sessionLocked(buffer, session)) {
            PedestalBlockUtil.changeBlockState(buffer.getExternalData().getWorld(), obeliskPos,
                    LOCKED_BLOCK_STATE);
        }
    }

    @Nullable
    public static String soleSlotKey(HexcodeSessionComponent session) {
        ImbuementProfileAsset profile = session.getProfile();
        if (profile == null) return null;
        Map<String, PedestalSlot> slots = profile.resolveSlots(session.getStoredItem());
        return slots.isEmpty() ? null : slots.keySet().iterator().next();
    }

    public static boolean sessionLocked(CommandBuffer<EntityStore> buffer,
            HexcodeSessionComponent session) {
        String slotKey = soleSlotKey(session);
        if (slotKey == null) return false;
        if (session.peekObeliskState(HANDLER_ID) instanceof EncryptionSessionState state
                && state.isUnlocked()) {
            return false;
        }
        Hex stored = session.getHexAt(slotKey, buffer);
        return stored != null && stored.getEncoding() != null && !stored.getEncoding().isEmpty();
    }

    @Override
    public void onExitCrafting(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            ObeliskBlockComponent obelisk) {
        HexcodeSessionComponent session = resolveSession(buffer, obelisk);
        if (session == null) return;
        if (session.peekObeliskState(HANDLER_ID) instanceof EncryptionSessionState state) {
            EncodingDisplay.despawn(buffer, state);
        }
    }

    @Nullable
    public static Vector3i boundPosition(PedestalBlockComponent pedestal,
            CommandBuffer<EntityStore> buffer) {
        World world = buffer.getExternalData().getWorld();
        for (Vector3i pos : pedestal.getActiveObelisks()) {
            if (pos == null) continue;
            ObeliskBlockComponent obelisk = BlockModule.getComponent(
                    ObeliskBlockComponent.getComponentType(), world, pos.x, pos.y, pos.z);
            if (obelisk != null && HANDLER_ID.equals(obelisk.getHandlerId())) {
                return pos;
            }
        }
        return null;
    }

    public static boolean bound(PedestalBlockComponent pedestal, CommandBuffer<EntityStore> buffer) {
        return boundPosition(pedestal, buffer) != null;
    }

    @Nullable
    public static List<EncodingStroke> liveEncoding(CommandBuffer<EntityStore> buffer,
            HexcodeSessionComponent session) {
        Ref<EntityStore> containerRef = session.getActiveContainerRef();
        if (containerRef == null || !containerRef.isValid()) return null;
        HexComponent hexComp = buffer.getComponent(containerRef, HexComponent.getComponentType());
        Hex hex = hexComp != null ? hexComp.getHex() : null;
        return hex != null ? hex.getEncoding() : null;
    }

    @Nullable
    private static HexcodeSessionComponent resolveSession(CommandBuffer<EntityStore> buffer,
            ObeliskBlockComponent obelisk) {
        Vector3i pedestalLoc = obelisk.getRegisteredPedestalLoc();
        if (pedestalLoc == null) return null;
        World world = buffer.getExternalData().getWorld();
        PedestalBlockComponent pedestal = BlockModule.getComponent(
                PedestalBlockComponent.getComponentType(), world,
                pedestalLoc.x, pedestalLoc.y, pedestalLoc.z);
        return pedestal != null ? SessionUtils.resolveSession(pedestal, buffer) : null;
    }
}
