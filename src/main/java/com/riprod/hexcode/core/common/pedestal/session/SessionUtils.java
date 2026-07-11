package com.riprod.hexcode.core.common.pedestal.session;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalItemUtil;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.utils.CleanupUtils;

public class SessionUtils {

    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public static HexcodeSessionComponent createSession(CommandBuffer<EntityStore> buffer,
            PedestalBlockComponent pedestal, Vector3i pedestalLocation,
            Ref<EntityStore> ownerRef, boolean isOpen) {

        HexcodeSessionComponent session = new HexcodeSessionComponent(pedestalLocation, ownerRef, isOpen);

        Ref<EntityStore> anchorRef = pedestal.getAnchorRef();
        if (anchorRef != null && anchorRef.isValid()) {
            session.setAnchorEntityRef(anchorRef);
        }

        buffer.addComponent(ownerRef, HexcodeSessionComponent.getComponentType(), session);

        pedestal.setSessionRef(ownerRef);

        HexcasterCraftingComponent craftingComp = buffer.getComponent(ownerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            craftingComp.setSessionRef(ownerRef);
        }

        logger.atInfo().log("session created at %s, open=%s", pedestalLocation, isOpen);
        return session;
    }

    public static void joinSession(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> ownerRef, Ref<EntityStore> participantRef) {

        HexcodeSessionComponent session = buffer.getComponent(ownerRef,
                HexcodeSessionComponent.getComponentType());
        if (session == null) return;

        session.addParticipant(participantRef);

        HexcasterCraftingComponent craftingComp = buffer.getComponent(participantRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            craftingComp.setSessionRef(ownerRef);
        }

        logger.atInfo().log("player joined session at %s", session.getPedestalLocation());
    }

    public static void leaveSession(CommandBuffer<EntityStore> buffer, Ref<EntityStore> participantRef,
            World world) {

        HexcasterCraftingComponent craftingComp = buffer.getComponent(participantRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null || !craftingComp.hasActiveSession()) return;

        Ref<EntityStore> ownerRef = craftingComp.getSessionRef();
        HexcodeSessionComponent session = buffer.getComponent(ownerRef,
                HexcodeSessionComponent.getComponentType());
        if (session == null) {
            craftingComp.clear(buffer);
            return;
        }

        session.removeParticipant(participantRef);
        craftingComp.clear(buffer);

        boolean isOwner = session.isOwner(participantRef);
        boolean noParticipants = session.getParticipantRefs().isEmpty();

        if (isOwner || noParticipants) {
            endSession(buffer, ownerRef, world);
        }
    }

    public static void saveHexToBook(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef,
            HexcodeSessionComponent session) {

        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            return;
        }

        Ref<EntityStore> activeHexRef = session.getActiveContainerRef();
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

        ImbuementProfileAsset profile = session.getProfile();
        if (profile == null) {
            return;
        }

        stack = profile.writeHex(stack, slotKey, hex);
        session.setStoredItem(stack);
    }

    public static void despawnPreviewScene(CommandBuffer<EntityStore> buffer, HexcodeSessionComponent session) {
        for (Ref<EntityStore> hexRef : session.getHexPreviewRefs()) {
            if (hexRef == null || !hexRef.isValid()) continue;

            HexComponent hexComp = buffer.getComponent(hexRef, HexComponent.getComponentType());
            if (hexComp != null) {
                Map<String, Ref<EntityStore>> childRefs = hexComp.getChildGlyphRefs();
                if (childRefs != null) {
                    for (Ref<EntityStore> glyphRef : childRefs.values()) {
                        if (glyphRef == null || !glyphRef.isValid()) continue;
                        GlyphComponent glyphComp = buffer.getComponent(glyphRef, GlyphComponent.getComponentType());
                        if (glyphComp != null) {
                            CleanupUtils.safeRemoveEntities(buffer, glyphComp.getSlotEntityRefs());
                            glyphComp.getSlotEntityRefs().clear();
                        }
                        buffer.tryRemoveComponent(glyphRef, MountedComponent.getComponentType());
                        CleanupUtils.safeRemoveEntity(buffer, glyphRef);
                    }
                }
            }

            buffer.tryRemoveComponent(hexRef, MountedComponent.getComponentType());
            CleanupUtils.safeRemoveEntity(buffer, hexRef);
        }
        session.clearHexPreviewRefs();

        CleanupUtils.safeRemoveEntities(buffer, session.getSlotNodeRefs());
        session.setSlotNodeRefs(new ArrayList<>());
    }

    public static void endSession(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ownerRef,
            World world) {

        if (ownerRef == null || !ownerRef.isValid()) return;

        HexcodeSessionComponent session = buffer.getComponent(ownerRef,
                HexcodeSessionComponent.getComponentType());
        if (session == null) return;

        // the crafting container lives outside the preview list after handoff; fold it
        // back in so the shared sweep below covers it
        Ref<EntityStore> activeContainer = session.getActiveContainerRef();
        if (activeContainer != null && !session.getHexPreviewRefs().contains(activeContainer)) {
            session.getHexPreviewRefs().add(activeContainer);
        }
        session.setActiveContainerRef(null);

        logger.atInfo().log("ending session at %s", session.getPedestalLocation());

        Set<Ref<EntityStore>> participants = session.getParticipantRefs();
        for (Ref<EntityStore> pRef : new ArrayList<>(participants)) {
            if (pRef == null || !pRef.isValid()) continue;
            CasterComponent caster = buffer.getComponent(pRef, CasterComponent.getComponentType());
            if (caster != null && caster.getCurrentContext() != null) {
                ContextTransitionService.exit(buffer, pRef, caster.getCurrentContext());
            }
            HexcasterCraftingComponent craftingComp = buffer.getComponent(pRef,
                    HexcasterCraftingComponent.getComponentType());
            if (craftingComp != null) {
                craftingComp.clear(buffer);
            }
        }
        participants.clear();

        despawnPreviewScene(buffer, session);

        Ref<EntityStore> anchorNodeRef = session.getAnchorNodeRef();
        if (anchorNodeRef != null && anchorNodeRef.isValid()) {
            buffer.tryRemoveEntity(anchorNodeRef, RemoveReason.REMOVE);
            session.setAnchorNodeRef(null);
        }

        ItemStack itemStack = session.getStoredItem();
        if (itemStack != null && !itemStack.isEmpty()) {
            PedestalItemUtil.returnBookToPlayer(buffer, ownerRef, itemStack, session.getSourceSlot());
            session.setStoredItem(ItemStack.EMPTY);
        }

        Ref<EntityStore> displayRef = session.getImbuedItemDisplayRef();
        if (displayRef != null && displayRef.isValid()) {
            buffer.tryRemoveEntity(displayRef, RemoveReason.REMOVE);
            session.setImbuedItemDisplayRef(null);
        }

        Vector3i pedestalLoc = session.getPedestalLocation();
        PedestalBlockComponent pedestal = BlockModule.getComponent(
                PedestalBlockComponent.getComponentType(), world,
                pedestalLoc.x, pedestalLoc.y, pedestalLoc.z);
        if (pedestal != null) {
            PedestalSystem.updateState(buffer, pedestal, session, world, PedestalState.IDLE);
            pedestal.setSessionRef(null);
            pedestal.setBookAssetId(null);
        }

        session.setOwnerRef(null);
        session.setActiveSlotKey(null);
        session.setAnchorEntityRef(null);

        buffer.tryRemoveComponent(ownerRef, HexcodeSessionComponent.getComponentType());
    }

    @Nullable
    public static HexcodeSessionComponent resolveSession(PedestalBlockComponent pedestal,
            ComponentAccessor<EntityStore> accessor) {
        Ref<EntityStore> ownerRef = pedestal.getSessionRef();
        if (ownerRef == null || !ownerRef.isValid()) return null;
        return accessor.getComponent(ownerRef, HexcodeSessionComponent.getComponentType());
    }

    @Nullable
    public static Ref<EntityStore> getSessionRef(PedestalBlockComponent pedestal) {
        Ref<EntityStore> ownerRef = pedestal.getSessionRef();
        if (ownerRef == null || !ownerRef.isValid()) return null;
        return ownerRef;
    }

    @Nullable
    public static HexcodeSessionComponent resolveSessionByPlayer(Ref<EntityStore> playerRef,
            ComponentAccessor<EntityStore> accessor) {
        HexcodeSessionComponent session = accessor.getComponent(playerRef,
                HexcodeSessionComponent.getComponentType());
        if (session != null) return session;
        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null || !craftingComp.hasActiveSession()) return null;
        return accessor.getComponent(craftingComp.getSessionRef(), HexcodeSessionComponent.getComponentType());
    }

    @Nullable
    public static Ref<EntityStore> findPreviewForSlot(HexcodeSessionComponent session, String slotKey) {
        if (session == null || slotKey == null) return null;
        ImbuementProfileAsset profile = session.getProfile();
        if (profile == null) return null;
        List<Ref<EntityStore>> previews = session.getHexPreviewRefs();
        int i = 0;
        for (Map.Entry<String, SlotAsset> entry : profile.resolveSlots(session.getStoredItem()).entrySet()) {
            if (i >= previews.size()) break;
            if (slotKey.equals(entry.getKey())) return previews.get(i);
            i++;
        }
        return null;
    }

    @Nullable
    public static Ref<EntityStore> getSessionRefByPlayer(Ref<EntityStore> playerRef,
            ComponentAccessor<EntityStore> accessor) {
        HexcodeSessionComponent session = accessor.getComponent(playerRef,
                HexcodeSessionComponent.getComponentType());
        if (session != null) return playerRef;
        HexcasterCraftingComponent craftingComp = accessor.getComponent(playerRef,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp == null || !craftingComp.hasActiveSession()) return null;
        return craftingComp.getSessionRef();
    }
}
