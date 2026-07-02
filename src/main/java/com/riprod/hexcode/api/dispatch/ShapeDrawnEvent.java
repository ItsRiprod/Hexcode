package com.riprod.hexcode.api.dispatch;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ShapeDrawnEvent extends CancellableEcsEvent {

    private final Ref<EntityStore> player;
    private final ShapeStructure structure;

    public ShapeDrawnEvent(Ref<EntityStore> player, ShapeStructure structure) {
        this.player = player;
        this.structure = structure;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }

    public ShapeStructure getStructure() {
        return structure;
    }
}
