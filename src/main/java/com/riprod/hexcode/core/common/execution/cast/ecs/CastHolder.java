package com.riprod.hexcode.core.common.execution.cast.ecs;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.CastOverlay;

public final class CastHolder {

    private final Ref<EntityStore> root;
    private final Map<CastComponentType<?>, CastComponent> supplied = new IdentityHashMap<>();
    private final Map<CastComponentType<?>, List<CastOverlay<?>>> overlays = new IdentityHashMap<>();

    public CastHolder(@Nonnull Ref<EntityStore> root) {
        this.root = root;
    }

    @Nonnull
    public Ref<EntityStore> getRoot() {
        return root;
    }

    @Nonnull
    public <T extends CastComponent> CastHolder put(@Nonnull CastComponentType<T> type, @Nonnull T component) {
        supplied.put(type, component);
        return this;
    }

    @Nonnull
    public <T extends CastComponent> CastHolder overlay(@Nonnull CastComponentType<T> type,
            @Nonnull CastOverlay<T> overlay) {
        overlays.computeIfAbsent(type, k -> new ArrayList<>(2)).add(overlay);
        return this;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CastComponent> T get(@Nonnull CastComponentType<T> type) {
        return (T) supplied.get(type);
    }

    @Nonnull
    public List<CastOverlay<?>> overlaysFor(@Nonnull CastComponentType<?> type) {
        var queued = overlays.get(type);
        return queued != null ? queued : List.of();
    }
}
