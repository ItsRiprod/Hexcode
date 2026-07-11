package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeEnterEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;

public class CraftingDrawModeEnterListener extends WorldEventSystem<EntityStore, DrawModeEnterEvent> {

    public CraftingDrawModeEnterListener() {
        super(DrawModeEnterEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull DrawModeEnterEvent event) {
        Ref<EntityStore> player = event.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }
        if (buffer.getComponent(player, CraftingState.getComponentType()) == null) {
            return;
        }
        DrawCaptureComponent capture = buffer.getComponent(player, DrawCaptureComponent.getComponentType());
        if (capture != null) {
            capture.setFinalizeDelaySeconds(-1f);
        }
    }
}
