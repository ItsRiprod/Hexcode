package com.riprod.hexcode.core.common.execution.cast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class ResourcePoolComponent implements CastComponent {

    public static final String SEED_SOURCE = "@initial";

    private static CastComponentType<ResourcePoolComponent> componentType;

    public static CastComponentType<ResourcePoolComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(CastComponentType<ResourcePoolComponent> type) {
        componentType = type;
    }

    static final class SourcePool {
        float pooled;
        float basis;
    }

    private final Object2ObjectOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, SourcePool>>
            resources = new Object2ObjectOpenHashMap<>();

    public ResourcePoolComponent() {
    }

    public float getResource(String id) {
        Object2ObjectLinkedOpenHashMap<String, SourcePool> sub = this.resources.get(id);
        if (sub == null) return 0f;
        float total = 0f;
        for (SourcePool pool : sub.values()) total += pool.pooled;
        return total;
    }

    public float getBasis(String id, String source) {
        Object2ObjectLinkedOpenHashMap<String, SourcePool> sub = this.resources.get(id);
        if (sub == null) return 0f;
        SourcePool pool = sub.get(source);
        return pool == null ? 0f : pool.basis;
    }

    public void addResource(String id, String source, float amount) {
        if (id == null || amount == 0f) return;
        Object2ObjectLinkedOpenHashMap<String, SourcePool> sub = this.resources.get(id);
        if (sub == null) {
            if (amount < 0f) return;
            sub = new Object2ObjectLinkedOpenHashMap<>(4);
            this.resources.put(id, sub);
        }
        SourcePool pool = sub.get(source);
        if (pool == null) {
            if (amount < 0f) {
                if (sub.isEmpty()) this.resources.remove(id);
                return;
            }
            pool = new SourcePool();
            sub.put(source, pool);
        }
        pool.pooled = Math.max(0f, pool.pooled + amount);
        pool.basis = Math.max(0f, pool.basis + amount);
        prune(id, sub);
    }

    public float consumeResource(String id, String spender, float cap) {
        Object2ObjectLinkedOpenHashMap<String, SourcePool> sub = this.resources.get(id);
        if (sub == null || sub.isEmpty() || cap == 0f) return 0f;

        float total = 0f;
        int positives = 0;
        for (SourcePool pool : sub.values()) {
            if (pool.pooled > 0f) {
                total += pool.pooled;
                positives++;
            }
        }
        if (total <= 0f) return 0f;

        float taken;
        if (cap < 0f || cap >= total) {
            for (SourcePool pool : sub.values()) pool.pooled = 0f;
            taken = total;
        } else {
            float remainingTake = cap;
            float remainingPool = total;
            float acc = 0f;
            for (SourcePool pool : sub.values()) {
                float have = pool.pooled;
                if (have <= 0f) continue;
                float take;
                if (--positives == 0) {
                    take = Math.min(remainingTake, have);
                } else {
                    take = have * (remainingTake / remainingPool);
                    if (take > have) take = have;
                    if (take > remainingTake) take = remainingTake;
                }
                pool.pooled = have - take;
                acc += take;
                remainingTake -= take;
                remainingPool -= have;
            }
            taken = acc;
        }

        if (taken > 0f) relieveBasis(sub, spender, taken);
        prune(id, sub);
        return taken;
    }

    private static void relieveBasis(Object2ObjectLinkedOpenHashMap<String, SourcePool> sub,
            String spender, float taken) {
        float reliefPool = 0f;
        for (Map.Entry<String, SourcePool> entry : sub.entrySet()) {
            if (entry.getKey().equals(spender)) continue;
            reliefPool += entry.getValue().basis;
        }
        if (reliefPool <= 0f) return;
        float factor = taken >= reliefPool ? 0f : 1f - (taken / reliefPool);
        for (Map.Entry<String, SourcePool> entry : sub.entrySet()) {
            if (entry.getKey().equals(spender)) continue;
            entry.getValue().basis *= factor;
        }
    }

    private void prune(String id, Object2ObjectLinkedOpenHashMap<String, SourcePool> sub) {
        Iterator<Map.Entry<String, SourcePool>> it = sub.entrySet().iterator();
        while (it.hasNext()) {
            SourcePool pool = it.next().getValue();
            if (pool.pooled <= 0f && pool.basis <= 0f) it.remove();
        }
        if (sub.isEmpty()) this.resources.remove(id);
    }

    public Map<String, Float> getResources() {
        Map<String, Float> totals = new HashMap<>(this.resources.size());
        for (Map.Entry<String, Object2ObjectLinkedOpenHashMap<String, SourcePool>> entry
                : this.resources.entrySet()) {
            float total = 0f;
            for (SourcePool pool : entry.getValue().values()) total += pool.pooled;
            if (total > 0f) totals.put(entry.getKey(), total);
        }
        return totals;
    }

    public Map<String, Map<String, Float>> getResourcePools() {
        Map<String, Map<String, Float>> pools = new HashMap<>(this.resources.size());
        for (Map.Entry<String, Object2ObjectLinkedOpenHashMap<String, SourcePool>> entry
                : this.resources.entrySet()) {
            Map<String, Float> sources = new HashMap<>(entry.getValue().size());
            for (Map.Entry<String, SourcePool> src : entry.getValue().entrySet()) {
                if (src.getValue().pooled > 0f) sources.put(src.getKey(), src.getValue().pooled);
            }
            if (!sources.isEmpty()) pools.put(entry.getKey(), sources);
        }
        return pools;
    }

    @Nonnull
    @Override
    public ResourcePoolComponent copy() {
        ResourcePoolComponent copy = new ResourcePoolComponent();
        for (Map.Entry<String, Object2ObjectLinkedOpenHashMap<String, SourcePool>> entry
                : this.resources.entrySet()) {
            Object2ObjectLinkedOpenHashMap<String, SourcePool> sub =
                    new Object2ObjectLinkedOpenHashMap<>(entry.getValue().size());
            for (Map.Entry<String, SourcePool> src : entry.getValue().entrySet()) {
                SourcePool pool = new SourcePool();
                pool.pooled = src.getValue().pooled;
                pool.basis = src.getValue().basis;
                sub.put(src.getKey(), pool);
            }
            copy.resources.put(entry.getKey(), sub);
        }
        return copy;
    }
}
