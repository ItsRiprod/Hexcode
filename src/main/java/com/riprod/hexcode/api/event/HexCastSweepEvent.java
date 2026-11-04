package com.riprod.hexcode.api.event;

import java.util.List;

import com.hypixel.hytale.component.system.EcsEvent;
import com.riprod.hexcode.core.common.execution.cast.HexCast;

public class HexCastSweepEvent extends EcsEvent {

    private final List<HexCast> finished;
    private final List<HexCast> overCap;

    public HexCastSweepEvent(List<HexCast> finished, List<HexCast> overCap) {
        this.finished = finished;
        this.overCap = overCap;
    }

    public List<HexCast> getFinished() {
        return finished;
    }

    public List<HexCast> getOverCap() {
        return overCap;
    }
}
