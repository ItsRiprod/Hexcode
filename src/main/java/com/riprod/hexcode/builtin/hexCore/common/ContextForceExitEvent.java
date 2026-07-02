package com.riprod.hexcode.builtin.hexCore.common;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ContextForceExitEvent extends EcsEvent {

    private final Ref<EntityStore> player;

    public ContextForceExitEvent(Ref<EntityStore> player) {
        this.player = player;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }
}
