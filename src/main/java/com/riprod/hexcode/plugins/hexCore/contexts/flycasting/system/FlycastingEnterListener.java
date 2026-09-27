package com.riprod.hexcode.plugins.hexCore.contexts.flycasting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeEnterEvent;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.plugins.hexCore.contexts.flycasting.component.FlycastingState;

public class FlycastingEnterListener extends WorldEventSystem<EntityStore, DrawModeEnterEvent> {

    public FlycastingEnterListener() {
        super(DrawModeEnterEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull DrawModeEnterEvent event) {
        Ref<EntityStore> player = event.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }
        ContextTransitionService.attemptEnter(buffer, player,
                FlycastingState.CONTEXT_ID, FlycastingState.PRIORITY);
    }
}
