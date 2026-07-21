package com.riprod.hexcode.core.common.execution.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CasterStateComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, CasterStateComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, CasterStateComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, CasterStateComponent> getComponentType() {
        return componentType;
    }

    private boolean holdingPrimary = false;
    private Map<UUID, List<Ref<EntityStore>>> dependencies = new HashMap<>();
    private List<HexStats> activeTrackers = new ArrayList<>();

    public CasterStateComponent() {
    }

    public void registerActiveTracker(HexStats tracker) {
        if (activeTrackers == null)
            activeTrackers = new ArrayList<>();
        if (tracker == null)
            return;
        pruneCompletedTrackers();
        activeTrackers.add(tracker);
    }

    public void pruneCompletedTrackers() {
        if (activeTrackers == null || activeTrackers.isEmpty())
            return;
        activeTrackers.removeIf(t -> t == null || t.getCurrentVolatility() <= 0f
                || t.getActiveBranchCount() <= 0);
    }

    public int getActiveCount() {
        pruneCompletedTrackers();
        if (activeTrackers == null) return 0;
        int n = 0;
        for (HexStats t : activeTrackers) {
            if (t != null && t.getSlotKey() == null) n++;
        }
        return n;
    }

    public void evictOldest() {
        if (activeTrackers == null || activeTrackers.isEmpty())
            return;
        for (int i = 0; i < activeTrackers.size(); i++) {
            HexStats t = activeTrackers.get(i);
            if (t != null && t.getSlotKey() == null) {
                activeTrackers.remove(i);
                t.setVolatility(0f);
                UUID execId = t.getExecutionId();
                if (execId != null) dependencies.remove(execId);
                return;
            }
        }
    }

    public void fizzleSlot(@Nonnull String slotKey) {
        if (activeTrackers == null || activeTrackers.isEmpty()) return;
        pruneCompletedTrackers();
        for (HexStats t : activeTrackers) {
            if (t == null) continue;
            if (slotKey.equals(t.getSlotKey()) && t.getCurrentVolatility() > 0f) {
                t.setVolatility(0f);
                UUID execId = t.getExecutionId();
                if (execId != null) dependencies.remove(execId);
            }
        }
    }

    public List<HexStats> getActiveTrackers() {
        if (activeTrackers == null)
            activeTrackers = new ArrayList<>();
        return activeTrackers;
    }

    public void cancelAll(Ref<EntityStore> casterRef) {
        if (activeTrackers == null || activeTrackers.isEmpty())
            return;
        for (HexStats tracker : new ArrayList<>(activeTrackers)) {
            if (tracker == null)
                continue;
            if (tracker.getCurrentVolatility() <= 0f)
                continue;
            tracker.setVolatility(0f);
        }
    }

    public boolean isHoldingPrimary() {
        return holdingPrimary;
    }

    public void setHoldingPrimary(boolean holding) {
        this.holdingPrimary = holding;
    }

    public void addDependency(UUID hexId, Ref<EntityStore> dependent) {
        dependencies.computeIfAbsent(hexId, k -> new ArrayList<>()).add(dependent);
    }

    public Map<UUID, List<Ref<EntityStore>>> getDependencies() {
        return dependencies;
    }

    public List<Ref<EntityStore>> getDependenciesForHex(UUID hexId) {
        return dependencies.getOrDefault(hexId, Collections.emptyList());
    }

    public List<Ref<EntityStore>> getDependencyList() {
        return dependencies.values().stream().flatMap(List::stream).toList();
    }

    @Nonnull
    @Override
    public CasterStateComponent clone() {
        CasterStateComponent copy = new CasterStateComponent();
        copy.holdingPrimary = this.holdingPrimary;
        copy.dependencies = new HashMap<>();
        for (Map.Entry<UUID, List<Ref<EntityStore>>> entry : this.dependencies.entrySet()) {
            copy.dependencies.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        copy.activeTrackers = this.activeTrackers != null ? new ArrayList<>(this.activeTrackers) : new ArrayList<>();
        return copy;
    }
}
