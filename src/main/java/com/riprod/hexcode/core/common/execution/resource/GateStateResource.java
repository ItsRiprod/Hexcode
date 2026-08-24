package com.riprod.hexcode.core.common.execution.resource;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public final class GateStateResource implements Resource<EntityStore> {

    public static final long STOPPED = Long.MAX_VALUE;

    private static ResourceType<EntityStore, GateStateResource> resourceType;

    public static ResourceType<EntityStore, GateStateResource> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, GateStateResource> type) {
        resourceType = type;
    }

    private long globalExpiryMillis = 0L;
    private final Object2LongOpenHashMap<UUID> playerExpiryMillis = new Object2LongOpenHashMap<>();

    public GateStateResource() {
        this.playerExpiryMillis.defaultReturnValue(0L);
    }

    public void stopGlobal() {
        this.globalExpiryMillis = STOPPED;
    }

    public void timeoutGlobal(long expiryMillis) {
        this.globalExpiryMillis = expiryMillis;
    }

    public void timeoutPlayer(@Nonnull UUID player, long expiryMillis) {
        this.playerExpiryMillis.put(player, expiryMillis);
    }

    public void resumeGlobal() {
        this.globalExpiryMillis = 0L;
    }

    public void resumePlayer(@Nonnull UUID player) {
        this.playerExpiryMillis.removeLong(player);
    }

    public long getGlobalExpiryMillis() {
        return this.globalExpiryMillis;
    }

    public long getExpiryFor(@Nonnull UUID player) {
        long playerExpiry = this.playerExpiryMillis.getLong(player);
        long effective = this.globalExpiryMillis;
        if (playerExpiry > effective) {
            effective = playerExpiry;
        }
        return effective;
    }

    public boolean isGloballyGated(long nowMillis) {
        return this.globalExpiryMillis == STOPPED || this.globalExpiryMillis > nowMillis;
    }

    public boolean isCasterGated(@Nonnull UUID player, long nowMillis) {
        if (isGloballyGated(nowMillis)) {
            return true;
        }
        long playerExpiry = this.playerExpiryMillis.getLong(player);
        if (playerExpiry == 0L) {
            return false;
        }
        if (playerExpiry > nowMillis) {
            return true;
        }
        this.playerExpiryMillis.removeLong(player);
        return false;
    }

    @Nonnull
    @Override
    public Resource<EntityStore> clone() {
        return new GateStateResource();
    }
}
