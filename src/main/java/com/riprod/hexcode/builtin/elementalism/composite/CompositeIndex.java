package com.riprod.hexcode.builtin.elementalism.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.CaseInsensitiveHashStrategy;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public final class CompositeIndex {
    private static volatile Map<String, List<String>> compositesByTier = Map.of();

    private CompositeIndex() {
    }

    public static List<CompositeAsset> lookup(String tierId) {
        var ids = compositesByTier.get(tierId);
        if (ids == null) {
            return List.of();
        }
        var map = CompositeAsset.getAssetMap();
        var composites = new ArrayList<CompositeAsset>(ids.size());
        for (var id : ids) {
            var composite = map.getAsset(id);
            if (composite != null) {
                composites.add(composite);
            }
        }
        return composites;
    }

    public static void onLoaded(LoadedAssetsEvent<String, CompositeAsset, DefaultAssetMap<String, CompositeAsset>> event) {
        rebuild();
    }

    public static void onRemoved(RemovedAssetsEvent<String, CompositeAsset, DefaultAssetMap<String, CompositeAsset>> event) {
        rebuild();
    }

    private static void rebuild() {
        var sorted = new TreeMap<String, CompositeAsset>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(CompositeAsset.getAssetMap().getAssetMap());

        var index = new Object2ObjectOpenCustomHashMap<String, List<String>>(CaseInsensitiveHashStrategy.getInstance());
        for (var composite : sorted.values()) {
            for (var tierId : composite.getIncoming()) {
                index.computeIfAbsent(tierId, k -> new ArrayList<>()).add(composite.getId());
            }
        }
        compositesByTier = index;
    }
}
