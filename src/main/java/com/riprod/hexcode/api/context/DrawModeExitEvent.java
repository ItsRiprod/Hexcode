package com.riprod.hexcode.api.context;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;

public class DrawModeExitEvent extends EcsEvent {

    private final Ref<EntityStore> player;
    private final DrawCaptureComponent capture;

    public DrawModeExitEvent(Ref<EntityStore> player, @Nullable DrawCaptureComponent capture) {
        this.player = player;
        this.capture = capture;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }

    @Nullable
    public DrawCaptureComponent getCapture() {
        return capture;
    }
}
