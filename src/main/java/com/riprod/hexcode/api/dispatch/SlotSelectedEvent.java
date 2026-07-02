package com.riprod.hexcode.api.dispatch;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SlotSelectedEvent extends CancellableEcsEvent {

    private final Ref<EntityStore> player;
    private final String slotKey;
    private final Ref<EntityStore> containerRef;

    public SlotSelectedEvent(Ref<EntityStore> player, String slotKey, Ref<EntityStore> containerRef) {
        this.player = player;
        this.slotKey = slotKey;
        this.containerRef = containerRef;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public Ref<EntityStore> getContainerRef() {
        return containerRef;
    }
}
