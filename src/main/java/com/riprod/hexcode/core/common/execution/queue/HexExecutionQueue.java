package com.riprod.hexcode.core.common.execution.queue;

import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class HexExecutionQueue implements Resource<EntityStore> {

    public record PendingGlyph(String glyphId, HexContext ctx) {
    }

    private static final int INITIAL_CAPACITY = 1024;

    private static ResourceType<EntityStore, HexExecutionQueue> resourceType;

    public static ResourceType<EntityStore, HexExecutionQueue> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, HexExecutionQueue> type) {
        resourceType = type;
    }

    private ObjectArrayList<PendingGlyph> pending = new ObjectArrayList<>(INITIAL_CAPACITY);
    private ObjectArrayList<PendingGlyph> snapshot = new ObjectArrayList<>(INITIAL_CAPACITY);
    private final ObjectArrayList<PendingGlyph> deferred = new ObjectArrayList<>(INITIAL_CAPACITY);
    private long tick;

    public HexExecutionQueue() {
    }

    public void enqueue(@Nonnull PendingGlyph item) {
        pending.add(item);
    }

    public void defer(@Nonnull PendingGlyph item) {
        deferred.add(item);
    }

    @Nonnull
    public ObjectArrayList<PendingGlyph> beginDrain() {
        ObjectArrayList<PendingGlyph> swap = pending;
        pending = snapshot;
        snapshot = swap;
        return snapshot;
    }

    public void endDrain(int consumed) {
        int size = snapshot.size();
        int cut = Math.min(consumed, size);
        if (cut == size && deferred.isEmpty()) {
            snapshot.clear();
            return;
        }
        snapshot.removeElements(0, cut);
        snapshot.addAll(snapshot.size(), deferred);
        snapshot.addAll(snapshot.size(), pending);
        ObjectArrayList<PendingGlyph> swap = pending;
        pending = snapshot;
        snapshot = swap;
        snapshot.clear();
        deferred.clear();
    }

    public long nextTick() {
        return ++tick;
    }

    public long getTick() {
        return tick;
    }

    public int size() {
        return pending.size();
    }

    public void clear() {
        pending.clear();
        snapshot.clear();
        deferred.clear();
    }

    public int removeIf(@Nonnull Predicate<PendingGlyph> filter) {
        int before = pending.size() + snapshot.size() + deferred.size();
        pending.removeIf(filter);
        snapshot.removeIf(filter);
        deferred.removeIf(filter);
        return before - pending.size() - snapshot.size() - deferred.size();
    }

    @Nullable
    @Override
    public Resource<EntityStore> clone() {
        return new HexExecutionQueue();
    }
}
