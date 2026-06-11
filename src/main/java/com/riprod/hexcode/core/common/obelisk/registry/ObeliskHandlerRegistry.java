package com.riprod.hexcode.core.common.obelisk.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.obelisk.interfaces.ObeliskInterface;

public class ObeliskHandlerRegistry {

    private static final Map<String, ObeliskInterface> handlers = new HashMap<>();

    private ObeliskHandlerRegistry() {
    }

    public static void register(@Nonnull String id, @Nonnull ObeliskInterface handler) {
        if (handlers.containsKey(id)) {
            throw new IllegalArgumentException("duplicate obelisk handler id: " + id);
        }
        handlers.put(id, handler);
    }

    @Nullable
    public static ObeliskInterface get(@Nonnull String id) {
        return handlers.get(id);
    }

    @Nonnull
    public static Map<String, ObeliskInterface> getAll() {
        return Collections.unmodifiableMap(new HashMap<>(handlers));
    }
}
