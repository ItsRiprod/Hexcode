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
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public final class HexCastStore implements Resource<EntityStore> {

    private static ResourceType<EntityStore, HexCastStore> resourceType;

    public static ResourceType<EntityStore, HexCastStore> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, HexCastStore> type) {
        resourceType = type;
    }

    private final Object2ObjectLinkedOpenHashMap<UUID, HexCast> casts = new Object2ObjectLinkedOpenHashMap<>();

    public HexCastStore() {
    }

    public void register(@Nonnull HexCast cast) {
        casts.put(cast.getExecutionId(), cast);
    }

    @Nullable
    public HexCast get(@Nullable UUID executionId) {
        return executionId == null ? null : casts.get(executionId);
    }

    public boolean remove(@Nullable UUID executionId) {
        return executionId != null && casts.remove(executionId) != null;
    }

    public int size() {
        return casts.size();
    }

    @Nonnull
    public List<HexCast> collectFinished() {
        List<HexCast> finished = null;
        for (var cast : casts.values()) {
            if (isFinished(cast)) {
                if (finished == null)
                    finished = new ArrayList<>(2);
                finished.add(cast);
            }
        }
        return finished == null ? List.of() : finished;
    }

    @Nonnull
    public List<HexCast> collectOverCap(int maxActive) {
        var excess = -maxActive;
        for (var cast : casts.values()) {
            if (!isFinished(cast))
                excess++;
        }
        if (excess <= 0) {
            return List.of();
        }
        List<HexCast> evicted = new ArrayList<>(excess);
        for (var cast : casts.values()) {
            if (evicted.size() == excess)
                break;
            if (!isFinished(cast))
                evicted.add(cast);
        }
        return evicted;
    }

    private static boolean isFinished(@Nonnull HexCast cast) {
        return cast.getActiveBranchCount() <= 0
                || cast.get(VolatilityComponent.TYPE).getCurrent() <= 0f;
    }

    @Nonnull
    @Override
    public Resource<EntityStore> clone() {
        var copy = new HexCastStore();
        for (var entry : casts.object2ObjectEntrySet()) {
            copy.casts.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }
}
