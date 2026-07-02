package com.riprod.hexcode.api.context;

import java.util.List;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class DrawModeEnterEvent extends EcsEvent {

    private final Ref<EntityStore> player;
    private final List<Hex> palette;

    public DrawModeEnterEvent(Ref<EntityStore> player, List<Hex> palette) {
        this.player = player;
        this.palette = palette;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }

    public List<Hex> getPalette() {
        return palette;
    }
}
