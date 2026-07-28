package com.riprod.hexcode.core.common.execution.queue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;

public final class HexExecutionQueue implements Resource<EntityStore> {

    public record PendingGlyph(String glyphId, HexContext ctx) {
    }

    private static ResourceType<EntityStore, HexExecutionQueue> resourceType;

    public static ResourceType<EntityStore, HexExecutionQueue> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, HexExecutionQueue> type) {
        resourceType = type;
    }

    private final Deque<PendingGlyph> pending = new ArrayDeque<>();

    public HexExecutionQueue() {
    }

    public void enqueue(PendingGlyph item) {
        pending.add(item);
    }

    public int size() {
        return pending.size();
    }

    public void clear() {
        pending.clear();
    }

    public int removeIf(Predicate<PendingGlyph> filter) {
        int before = pending.size();
        pending.removeIf(filter);
        return before - pending.size();
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
