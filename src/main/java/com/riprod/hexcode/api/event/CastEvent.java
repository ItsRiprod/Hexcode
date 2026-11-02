package com.riprod.hexcode.api.event;

import java.util.function.Consumer;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.event.IEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public class CastEvent implements IEvent<Void>, ICancellable {

    private final HexContext context;
    private boolean cancelled;

    public CastEvent(HexContext context) {
        this.context = context;
    }

    public HexContext getContext() {
        return context;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static void register(EventRegistry registry, Consumer<CastEvent> handler) {
        register(registry, EventPriority.NORMAL.getValue(), handler);
    }

    public static void register(EventRegistry registry, short priority, Consumer<CastEvent> handler) {
        registry.register(priority, CastEvent.class, e -> {
            if (!e.isCancelled()) handler.accept(e);
        });
    }

    public static void registerIncludingCancelled(EventRegistry registry, short priority,
            Consumer<CastEvent> handler) {
        registry.register(priority, CastEvent.class, handler);
    }
}
