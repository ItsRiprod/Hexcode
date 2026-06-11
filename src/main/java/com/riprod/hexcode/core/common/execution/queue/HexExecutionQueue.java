package com.riprod.hexcode.core.common.execution.queue;

import java.util.ArrayDeque;
import java.util.Deque;

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
