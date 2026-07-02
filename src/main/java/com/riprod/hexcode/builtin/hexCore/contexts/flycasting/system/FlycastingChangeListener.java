package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.HexContextChangeEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.FlycastingScene;
import com.riprod.hexcode.core.common.context.ContextTransitionService;

public class FlycastingChangeListener extends WorldEventSystem<EntityStore, HexContextChangeEvent> {

    public FlycastingChangeListener() {
        super(HexContextChangeEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContextChangeEvent event) {
        Ref<EntityStore> player = event.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }

        if (FlycastingState.CONTEXT_ID.equals(event.getNewContextId())) {
            FlycastingState state = FlycastingScene.spawn(buffer, player);
            if (state == null) {
                ContextTransitionService.exit(buffer, player, FlycastingState.CONTEXT_ID);
                return;
            }
            buffer.putComponent(player, FlycastingState.getComponentType(), state);
            return;
        }

        FlycastingState state = buffer.getComponent(player, FlycastingState.getComponentType());
        if (state != null) {
            FlycastingScene.teardown(buffer, state);
            buffer.tryRemoveComponent(player, FlycastingState.getComponentType());
        }
    }
}
