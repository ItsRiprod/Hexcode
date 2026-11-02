package com.riprod.hexcode.api.event;

import java.util.function.Consumer;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.riprod.hexcode.core.common.execution.cast.HexCast;

public class CastEndEvent implements IEvent<Void> {

    private final HexCast cast;
    private final World world;

    public CastEndEvent(HexCast cast, World world) {
        this.cast = cast;
        this.world = world;
    }

    public HexCast getCast() {
        return cast;
    }

    public World getWorld() {
        return world;
    }

    public static void register(EventRegistry registry, Consumer<CastEndEvent> handler) {
        register(registry, EventPriority.NORMAL.getValue(), handler);
    }

    public static void register(EventRegistry registry, short priority, Consumer<CastEndEvent> handler) {
        registry.register(priority, CastEndEvent.class, handler);
    }
}
