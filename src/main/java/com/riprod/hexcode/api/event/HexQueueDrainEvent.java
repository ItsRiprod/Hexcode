package com.riprod.hexcode.api.event;

import com.hypixel.hytale.component.system.EcsEvent;

public class HexQueueDrainEvent extends EcsEvent {

    private final int count;

    public HexQueueDrainEvent(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
