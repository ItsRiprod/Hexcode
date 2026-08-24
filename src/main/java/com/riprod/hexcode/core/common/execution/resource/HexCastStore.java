package com.riprod.hexcode.core.common.execution.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public final class HexCastStore implements Resource<EntityStore> {

    private static ResourceType<EntityStore, HexCastStore> resourceType;

    public static ResourceType<EntityStore, HexCastStore> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, HexCastStore> type) {
        resourceType = type;
    }

    private static final class Entry {
        private final HexCast cast;
        private final long registeredTick;

        private Entry(HexCast cast, long registeredTick) {
            this.cast = cast;
            this.registeredTick = registeredTick;
        }
    }

    private final Object2ObjectLinkedOpenHashMap<UUID, Entry> entries = new Object2ObjectLinkedOpenHashMap<>();
    private long tick;

    public HexCastStore() {
    }

    public void register(@Nonnull HexCast cast) {
        entries.put(cast.getExecutionId(), new Entry(cast, tick));
    }

    @Nullable
    public HexCast get(@Nullable UUID executionId) {
        if (executionId == null)
            return null;
        Entry entry = entries.get(executionId);
        return entry != null ? entry.cast : null;
    }

    public boolean remove(@Nullable UUID executionId) {
        return executionId != null && entries.remove(executionId) != null;
    }

    public int size() {
        return entries.size();
    }

    public long nextTick() {
        return ++tick;
    }

    public long getTick() {
        return tick;
    }

    public int reclaimFinished() {
        int removed = 0;
        var it = entries.object2ObjectEntrySet()
                .fastIterator();
        while (it.hasNext()) {
            HexCast cast = it.next().getValue().cast;
            if (isFinished(cast)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    @Nonnull
    public List<HexCast> endExpired(long maxLifetimeTicks) {
        List<HexCast> expired = null;
        var it = entries.object2ObjectEntrySet().fastIterator();
        while (it.hasNext()) {
            Entry entry = it.next().getValue();
            if (tick - entry.registeredTick < maxLifetimeTicks) {
                break;
            }
            if (expired == null)
                expired = new ArrayList<>(2);
            expired.add(end(entry.cast));
            it.remove();
        }
        return expired == null ? List.of() : expired;
    }

    @Nonnull
    public List<HexCast> endOverCap(int maxActive) {
        if (entries.size() <= maxActive) {
            return List.of();
        }
        List<HexCast> evicted = new ArrayList<>(entries.size() - maxActive);
        while (entries.size() > maxActive) {
            evicted.add(end(entries.removeFirst().cast));
        }
        return evicted;
    }

    @Nonnull
    private static HexCast end(@Nonnull HexCast cast) {
        cast.volatility().setCurrent(0f);
        return cast;
    }

    private static boolean isFinished(@Nullable HexCast cast) {
        return cast == null || cast.getActiveBranchCount() <= 0
                || cast.volatility().getCurrent() <= 0f;
    }

    @Nonnull
    @Override
    public Resource<EntityStore> clone() {
        HexCastStore copy = new HexCastStore();
        copy.tick = this.tick;
        for (var entry : this.entries.object2ObjectEntrySet()) {
            Entry value = entry.getValue();
            copy.entries.put(entry.getKey(), new Entry(value.cast.copy(), value.registeredTick));
        }
        return copy;
    }
}
