package com.riprod.hexcode.api.event;

import java.util.function.Consumer;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public class BranchEndEvent implements IEvent<Void> {

    public enum Reason {
        NATURAL,
        STARVED,
        INTERRUPTED,
        ERROR
    }

    private final HexContext context;
    private final Reason reason;
    private final CommandBuffer<EntityStore> buffer;

    public BranchEndEvent(HexContext context, Reason reason, CommandBuffer<EntityStore> buffer) {
        this.context = context;
        this.reason = reason;
        this.buffer = buffer;
    }

    public HexContext getContext() {
        return context;
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

    public static void register(EventRegistry registry, Consumer<BranchEndEvent> handler) {
        register(registry, EventPriority.NORMAL.getValue(), handler);
    }

    public static void register(EventRegistry registry, short priority, Consumer<BranchEndEvent> handler) {
        registry.register(priority, BranchEndEvent.class, handler);
    }
}
