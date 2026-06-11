package com.riprod.hexcode.api.event;

import com.hypixel.hytale.component.system.EcsEvent;

public class HexDrainEvent extends EcsEvent {

    private final int count;

    public HexDrainEvent(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
