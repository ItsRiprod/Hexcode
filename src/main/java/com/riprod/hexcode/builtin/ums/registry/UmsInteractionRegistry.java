package com.riprod.hexcode.builtin.ums.registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UmsInteractionRegistry {
    private static final Map<String, UmsInteractionHandler> handlers = new HashMap<>();

    private UmsInteractionRegistry() {
    }

    public static void register(@Nonnull UmsInteractionHandler handler) {
        UmsInteractionHandler existing = handlers.get(handler.getId());
        if (existing != null) {
            throw new IllegalArgumentException("duplicate ums interaction handler id: " + handler.getId());
        }
        handlers.put(handler.getId(), handler);
    }

    @Nullable
    public static UmsInteractionHandler get(@Nonnull String handlerId) {
        return handlers.get(handlerId);
    }

    @Nonnull
    public static Map<String, UmsInteractionHandler> getAll() {
        return Collections.unmodifiableMap(new HashMap<>(handlers));
    }
}
