package com.riprod.hexcode.core.common.hexes.codec;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public final class HexCacheResource implements Resource<EntityStore> {

    private static final int MAX_ENTRIES = 1024;

    private static ResourceType<EntityStore, HexCacheResource> resourceType;

    public static ResourceType<EntityStore, HexCacheResource> getResourceType() {
        return resourceType;
    }

    public static void setResourceType(ResourceType<EntityStore, HexCacheResource> type) {
        resourceType = type;
    }

    private final Map<String, Hex> cache = new LinkedHashMap<>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Hex> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public HexCacheResource() {
    }

    @Nullable
    public Hex getOrDecode(String data) {
        if (data == null)
            return null;
        Hex cached = cache.get(data);
        if (cached != null)
            return cached.clone();
        DecodeResult r = HexCodec.deserialize(data);
        Hex hex = r.getHex();
        if (hex == null)
            return null;
        cache.put(data, hex);
        return hex.clone();
    }

    public int size() {
        return cache.size();
    }

    public void invalidate() {
        cache.clear();
    }

    @Nullable
    @Override
    public Resource<EntityStore> clone() {
        return new HexCacheResource();
    }
}
