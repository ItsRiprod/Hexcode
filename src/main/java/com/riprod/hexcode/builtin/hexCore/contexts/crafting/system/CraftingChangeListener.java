package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.joml.Vector3d;

import com.riprod.hexcode.api.context.HexContextChangeEvent;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.builtin.hexCore.nodes.anchor.AnchorNodeHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.container.ContainerNodeHandler;
import com.riprod.hexcode.builtin.hexCore.nodes.slot.SlotNodeHandler;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.obelisk.system.ObeliskDispatcher;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;
import com.riprod.hexcode.builtin.hexCore.scene.CraftingDragHandler;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class CraftingChangeListener extends WorldEventSystem<EntityStore, HexContextChangeEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public CraftingChangeListener() {
        super(HexContextChangeEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContextChangeEvent event) {
        Ref<EntityStore> player = event.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }

        if (CraftingState.CONTEXT_ID.equals(event.getNewContextId())) {
            enter(buffer, player);
            return;
        }

        CraftingState state = buffer.getComponent(player, CraftingState.getComponentType());
        if (state == null) {
            return;
        }
        buffer.tryRemoveComponent(player, CraftingState.getComponentType());

        HexcasterCraftingComponent craftingComp = buffer.getComponent(player,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            CraftingDragHandler.endDrag(buffer, craftingComp.getDraggingRef(),
                    craftingComp.getHeadAnchorRef(), craftingComp);
        }
        GravityUtil.exitFly(buffer, player);

        Ref<EntityStore> sessionRef = SessionUtils.getSessionRefByPlayer(player, buffer);
        HexcodeSessionComponent session = sessionRef != null
                ? buffer.getComponent(sessionRef, HexcodeSessionComponent.getComponentType())
                : null;
        if (session == null) {
            if (event.getNewContextId() == null) {
                ContextTransitionService.setInContextStat(buffer, player, false);
            }
            return;
        }

        if (!session.isOwner(player)) {
            session.removeParticipant(player);
            if (craftingComp != null) {
                craftingComp.clear(buffer);
            }
            if (event.getNewContextId() == null) {
                ContextTransitionService.setInContextStat(buffer, player, false);
            }
            return;
        }

        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        teardownScene(buffer, player, session, pedestal);
        if (craftingComp != null) {
            craftingComp.clearCraftingState();
        }

        if (SelectingState.CONTEXT_ID.equals(event.getNewContextId())) {
            return;
        }
        ContextTransitionService.setInContextStat(buffer, player, false);
        HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                .dispatch(CraftingEvent.builder(CraftingEvent.Reason.EXITED_NORMAL, player)
                        .pedestalLocation(session.getPedestalLocation())
                        .build());
        SessionUtils.endSession(buffer, sessionRef, buffer.getExternalData().getWorld());
    }

    private static void enter(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        GravityUtil.enterFly(buffer, player);
        buffer.putComponent(player, CraftingState.getComponentType(), new CraftingState());
        ContextTransitionService.setInContextStat(buffer, player, true);

        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        HexcodeSessionComponent session = pedestal != null
                ? SessionUtils.resolveSession(pedestal, buffer)
                : null;
        if (pedestal == null || session == null || !session.isOwner(player)) {
            return;
        }

        Ref<EntityStore> containerRef = session.getActiveContainerRef();
        String slotKey = session.getActiveSlotKey();
        if (containerRef == null || !containerRef.isValid() || slotKey == null) {
            return;
        }

        Hex hex = ContainerNodeHandler.INSTANCE.prepareForCrafting(buffer, containerRef, session, slotKey);

        Vector3d anchorPos = PedestalEntity.getAnchorPosition(session.getPedestalLocation());
        Vector3d activePos = new Vector3d(
                anchorPos.x + PedestalSystem.ACTIVE_HEX_OFFSET.x,
                anchorPos.y + PedestalSystem.ACTIVE_HEX_OFFSET.y,
                anchorPos.z + PedestalSystem.ACTIVE_HEX_OFFSET.z);
        TransformComponent transform = buffer.getComponent(containerRef, TransformComponent.getComponentType());
        if (transform != null) {
            transform.getPosition().set(activePos);
            transform.getRotation().set(0f, 0f, 0f);
        }
        if (buffer.getComponent(containerRef, MountedComponent.getComponentType()) != null) {
            buffer.removeComponent(containerRef, MountedComponent.getComponentType());
        }

        Ref<EntityStore> rootNodeRef = AnchorNodeHandler.INSTANCE.spawnNode(buffer, hex,
                containerRef, activePos, player);
        session.setAnchorNodeRef(rootNodeRef);

        HexcasterCraftingComponent craftingComp = buffer.getComponent(player,
                HexcasterCraftingComponent.getComponentType());
        if (craftingComp != null) {
            craftingComp.setHoveredRef(null);
        }

        World world = buffer.getExternalData().getWorld();
        ObeliskDispatcher.dispatchEnterCrafting(buffer, pedestal, player);
        HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                .dispatch(CraftingEvent.builder(CraftingEvent.Reason.ENTERED_CRAFTING, player)
                        .pedestal(pedestal)
                        .hex(hex)
                        .slotKey(slotKey)
                        .build());
        PedestalSystem.updateState(buffer, pedestal, session, world, PedestalState.CRAFTING);
    }

    private static void teardownScene(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            HexcodeSessionComponent session, @Nullable PedestalBlockComponent pedestal) {
        if (pedestal != null) {
            ObeliskDispatcher.dispatchExitCrafting(buffer, pedestal, player);
        }
        SessionUtils.saveHexToBook(buffer, player, session);

        SlotNodeHandler.INSTANCE.despawn(buffer, session);
        session.setSlotNodeRefs(new ArrayList<>());

        Ref<EntityStore> containerRef = session.getActiveContainerRef();
        if (containerRef != null && containerRef.isValid()) {
            HexComponent hexComp = buffer.getComponent(containerRef, HexComponent.getComponentType());
            if (hexComp != null) {
                Map<String, Ref<EntityStore>> children = hexComp.getChildGlyphRefs();
                if (children != null) {
                    for (Ref<EntityStore> childRef : children.values()) {
                        if (childRef == null || !childRef.isValid()) {
                            continue;
                        }
                        SlotNodeHandler.INSTANCE.despawnSlotsForGlyph(buffer, childRef);
                        buffer.tryRemoveComponent(childRef, MountedComponent.getComponentType());
                        buffer.tryRemoveEntity(childRef, RemoveReason.REMOVE);
                    }
                }
            }
            buffer.tryRemoveComponent(containerRef, MountedComponent.getComponentType());
            buffer.tryRemoveEntity(containerRef, RemoveReason.REMOVE);
        }
        session.setActiveContainerRef(null);

        Ref<EntityStore> anchorNodeRef = session.getAnchorNodeRef();
        if (anchorNodeRef != null && anchorNodeRef.isValid()) {
            buffer.tryRemoveEntity(anchorNodeRef, RemoveReason.REMOVE);
        }
        session.setAnchorNodeRef(null);
        session.setActiveSlotKey(null);
    }
}
