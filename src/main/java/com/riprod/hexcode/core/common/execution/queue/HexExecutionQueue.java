package com.riprod.hexcode.core.common.execution.queue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;

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

    private final Deque<PendingGlyph> pending = new ArrayDeque<>(INITIAL_CAPACITY);
    private final Deque<PendingGlyph> deferred = new ArrayDeque<>(INITIAL_CAPACITY);
    private long tick;

    public HexExecutionQueue() {
    }

    public void enqueue(@Nonnull PendingGlyph item) {
        pending.addLast(item);
    }

    public void defer(@Nonnull PendingGlyph item) {
        deferred.addLast(item);
    }

    public void restoreDeferred() {
        PendingGlyph item;
        while ((item = deferred.pollLast()) != null) {
            pending.addFirst(item);
        }
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
        deferred.clear();
    }

    public int removeIf(@Nonnull Predicate<PendingGlyph> filter) {
        int before = pending.size() + deferred.size();
        pending.removeIf(filter);
        deferred.removeIf(filter);
        return before - pending.size() - deferred.size();
    }

    @Nullable
    public PendingGlyph poll() {
        return pending.poll();
    }

    @Nullable
    @Override
    public Resource<EntityStore> clone() {
        return new HexExecutionQueue();
    }
}
