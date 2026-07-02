package com.riprod.hexcode.api.context;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DrawModeExitEvent extends EcsEvent {

    private final Ref<EntityStore> player;

    public DrawModeExitEvent(Ref<EntityStore> player) {
        this.player = player;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }
}
