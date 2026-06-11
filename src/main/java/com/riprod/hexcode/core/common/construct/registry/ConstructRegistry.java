package com.riprod.hexcode.core.common.construct.registry;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;

public class ConstructRegistry {

    private static final Map<String, ConstructHandler<?>> handlers = new HashMap<>();

    private ConstructRegistry() {
    }

    public static void register(@Nonnull String id, @Nonnull ConstructHandler<?> handler) {
        if (handlers.containsKey(id)) {
            throw new IllegalArgumentException("duplicate construct handler id: " + id);
        }
        handlers.put(id, handler);
    }

    @Nullable
    public static ConstructHandler<?> get(@Nonnull String id) {
        return handlers.get(id);
    }

    @Nonnull
    public static Map<String, ConstructHandler<?>> getAll() {
        return Map.copyOf(handlers);
    }
}
