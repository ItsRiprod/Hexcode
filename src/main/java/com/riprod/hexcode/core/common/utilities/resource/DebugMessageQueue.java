package com.riprod.hexcode.core.common.utilities.resource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class DebugMessageQueue implements Resource<EntityStore> {

    public static final int MAX_PENDING = 10;
    public static final float RETAIN_SECONDS = 2.0f;

    public static final class Entry {
        private final Deque<Message> pending = new ArrayDeque<>();
        private int dropped;
        private float idle;

        public Deque<Message> getPending() {
            return pending;
        }

        public int getDropped() {
            return dropped;
        }

        public void onFlushed() {
            pending.clear();
            dropped = 0;
            idle = 0f;
        }

        public boolean isExpired(float dt) {
            idle += dt;
            return idle >= RETAIN_SECONDS;
        }

        void append(Message message) {
            pending.addLast(message);
            while (pending.size() > MAX_PENDING) {
                pending.removeFirst();
                dropped++;
            }
        }
    }

    private static ResourceType<EntityStore, DebugMessageQueue> resourceType;

    public static ResourceType<EntityStore, DebugMessageQueue> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, DebugMessageQueue> type) {
        resourceType = type;
    }

    private final Map<Ref<EntityStore>, Entry> entries = new HashMap<>();

    public DebugMessageQueue() {
    }

    public void append(Ref<EntityStore> ref, Message message) {
        entries.computeIfAbsent(ref, r -> new Entry()).append(message);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public Map<Ref<EntityStore>, Entry> getEntries() {
        return entries;
    }

    @Nullable
    @Override
    public Resource<EntityStore> clone() {
        return new DebugMessageQueue();
    }
}
