package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastHolder;

/** Short-lived assembler for managing overlays */
public final class CastAssembler implements CastAssembly {

    private final CastHolder holder;
    private final CommandBuffer<EntityStore> buffer;
    private final CastComponent[] resolved = new CastComponent[HexComponentRegistry.size()];
    private final boolean[] done = new boolean[HexComponentRegistry.size()];
    private final boolean[] resolving = new boolean[HexComponentRegistry.size()];
    private boolean aborted;

    public CastAssembler(@Nonnull CastHolder holder, @Nonnull CommandBuffer<EntityStore> buffer) {
        this.holder = holder;
        this.buffer = buffer;
    }

    @Nonnull
    @Override
    public Ref<EntityStore> root() {
        return holder.getRoot();
    }

    @Nonnull
    @Override
    public CommandBuffer<EntityStore> accessor() {
        return buffer;
    }

    public boolean isAborted() {
        return aborted;
    }

    @Nullable
    @Override
    public <T extends CastComponent> T get(@Nonnull CastComponentType<T> type) {
        if (aborted) {
            return null;
        }
        var index = type.getIndex();
        if (done[index]) {
            return (T) resolved[index];
        }
        if (resolving[index]) {
            throw new IllegalStateException("cast component cycle at " + type.getId());
        }
        resolving[index] = true;
        try {
            var value = holder.get(type);
            if (value == null) {
                var hydrator = HexComponentRegistry.getHydrator(type);
                if (hydrator != null) {
                    value = (T) hydrator.hydrate(this);
                    if (value == null) {
                        aborted = true;
                        return null;
                    }
                }
            }

            if (value != null) {
                for (var layer : holder.overlaysFor(type)) {
                    ((CastOverlay) layer).applyTo(value);
                }
            }
            resolved[index] = value;
            done[index] = true;
            return value;
        } finally {
            resolving[index] = false;
        }
    }
}
