package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.HexContextChangeEvent;
import com.riprod.hexcode.api.event.CraftingEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.handlers.CraftingDragHandler;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.core.state.crafting.utils.GravityUtil;

public class CraftingChangeListener extends WorldEventSystem<EntityStore, HexContextChangeEvent> {

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
            buffer.ensureComponent(player, HexcasterCraftingComponent.getComponentType());
            GravityUtil.enterFly(buffer, player);
            buffer.putComponent(player, CraftingState.getComponentType(), new CraftingState());
            ContextTransitionService.setInContextStat(buffer, player, true);
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

        // returning to selecting keeps the session alive; the pedestal handler and
        // container node own that handoff
        if (SelectingState.CONTEXT_ID.equals(event.getNewContextId())) {
            return;
        }
        if (event.getNewContextId() == null) {
            ContextTransitionService.setInContextStat(buffer, player, false);
            finalizeSession(buffer, player, craftingComp);
        }
    }

    private static void finalizeSession(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            @Nullable HexcasterCraftingComponent craftingComp) {
        Ref<EntityStore> sessionRef = SessionUtils.getSessionRefByPlayer(player, buffer);
        if (sessionRef == null) {
            return;
        }
        HexcodeSessionComponent session = buffer.getComponent(sessionRef,
                HexcodeSessionComponent.getComponentType());
        if (session == null) {
            return;
        }

        if (!session.isOwner(player)) {
            session.removeParticipant(player);
            if (craftingComp != null) {
                craftingComp.clear(buffer);
            }
            return;
        }

        World world = buffer.getExternalData().getWorld();
        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        if (pedestal != null) {
            PedestalSystem.exitCrafting(buffer, player, pedestal, session);
        }
        HytaleServer.get().getEventBus().dispatchFor(CraftingEvent.class)
                .dispatch(CraftingEvent.builder(CraftingEvent.Reason.EXITED_NORMAL, player)
                        .pedestalLocation(session.getPedestalLocation())
                        .build());
        SessionUtils.endSession(buffer, sessionRef, world);
    }
}
