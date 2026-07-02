package com.riprod.hexcode.builtin.hexCore.contexts.selecting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.SlotSelectedEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;

public class SelectingSlotSelectSystem extends EntityEventSystem<EntityStore, SlotSelectedEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public SelectingSlotSelectSystem() {
        super(SlotSelectedEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return SelectingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull SlotSelectedEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        Ref<EntityStore> sessionRef = SessionUtils.getSessionRefByPlayer(player, buffer);
        HexcodeSessionComponent session = sessionRef != null
                ? buffer.getComponent(sessionRef, HexcodeSessionComponent.getComponentType())
                : null;
        if (session == null) {
            return;
        }

        // explicit handoff before the transition: crafting owns the selected container
        // via activeContainerRef, selecting keeps only the remainder in hexPreviewRefs,
        // so change-listener ordering inside the announce cannot cross-despawn scenes
        session.setActiveSlotKey(event.getSlotKey());
        session.setActiveContainerRef(event.getContainerRef());
        session.getHexPreviewRefs().remove(event.getContainerRef());

        ContextTransitionService.transitionFrom(buffer, player,
                SelectingState.CONTEXT_ID, CraftingState.CONTEXT_ID, CraftingState.PRIORITY);
    }
}
