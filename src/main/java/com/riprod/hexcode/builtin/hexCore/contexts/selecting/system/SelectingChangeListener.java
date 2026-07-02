package com.riprod.hexcode.builtin.hexCore.contexts.selecting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.HexContextChangeEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;

public class SelectingChangeListener extends WorldEventSystem<EntityStore, HexContextChangeEvent> {

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
            buffer.ensureComponent(player, HexcasterCraftingComponent.getComponentType());
            buffer.putComponent(player, SelectingState.getComponentType(), new SelectingState());
            ContextTransitionService.setInContextStat(buffer, player, true);
            return;
        }

        SelectingState state = buffer.getComponent(player, SelectingState.getComponentType());
        if (state == null) {
            return;
        }
        buffer.tryRemoveComponent(player, SelectingState.getComponentType());

        // headed to crafting: the session persists on the pedestal, the handler and
        // container node own the scene handoff
        if (CraftingState.CONTEXT_ID.equals(event.getNewContextId())) {
            return;
        }
        if (event.getNewContextId() == null) {
            ContextTransitionService.setInContextStat(buffer, player, false);
            endSessionIfOwner(buffer, player);
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
