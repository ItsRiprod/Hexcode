package com.riprod.hexcode.api.event;

import java.util.function.Consumer;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;

public class CastEndEvent implements IEvent<Void> {

    public enum Reason {
        COMPLETED,
        DEPLETED,
        EVICTED,
        CANCELLED,
        DISPELLED,
        CASTER_REMOVED
    }

    private final HexCast cast;
    private final Reason reason;
    private final CommandBuffer<EntityStore> buffer;

    public CastEndEvent(HexCast cast, Reason reason, CommandBuffer<EntityStore> buffer) {
        this.cast = cast;
        this.reason = reason;
        this.buffer = buffer;
    }

    public HexCast getCast() {
        return cast;
    }

    public Reason getReason() {
        return reason;
    }

    public CommandBuffer<EntityStore> getBuffer() {
        return buffer;
    }

    public World getWorld() {
        return buffer.getExternalData().getWorld();
    }

    public static void register(EventRegistry registry, Consumer<CastEndEvent> handler) {
        register(registry, EventPriority.NORMAL.getValue(), handler);
    }

    public static void register(EventRegistry registry, short priority, Consumer<CastEndEvent> handler) {
        registry.register(priority, CastEndEvent.class, handler);
    }
}
