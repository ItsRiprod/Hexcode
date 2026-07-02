package com.riprod.hexcode.api.context;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HexContextChangeEvent extends EcsEvent {

    private final Ref<EntityStore> player;
    private final String newContextId;

    public HexContextChangeEvent(Ref<EntityStore> player, String newContextId) {
        this.player = player;
        this.newContextId = newContextId;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }

    public String getNewContextId() {
        return newContextId;
    }
}
