package com.riprod.hexcode.core.common.execution.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.codec.HexFieldCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class HexcasterIdleComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, HexcasterIdleComponent> componentType;

    public static final BuilderCodec<HexcasterIdleComponent> CODEC = BuilderCodec
            .builder(HexcasterIdleComponent.class, HexcasterIdleComponent::new)
            .append(new KeyedCodec<>("Hex", HexFieldCodec.PLAYER),
                    (c, v) -> c.hex = v,
                    c -> c.hex)
            .add()
            .append(new KeyedCodec<>("CastCount", Codec.INTEGER),
                    (c, v) -> c.castCount = v,
                    c -> c.castCount)
            .add()
            .append(new KeyedCodec<>("CumulativeDecay", Codec.FLOAT),
                    (c, v) -> c.cumulativeDecay = v,
                    c -> c.cumulativeDecay)
            .add()
            .build();

    public HexcasterIdleComponent() {
    }

    public static void setComponentType(ComponentType<EntityStore, HexcasterIdleComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexcasterIdleComponent> getComponentType() {
        return componentType;
    }

    private Hex hex;
    private int castCount = 0;
    private float cumulativeDecay = 0f;
    private boolean holdingPrimary = false;
    private Map<UUID, List<Ref<EntityStore>>> dependencies = new HashMap<>();

    private transient List<VolatilityTracker> activeTrackers = new ArrayList<>();

    public void registerActiveTracker(VolatilityTracker tracker) {
        if (activeTrackers == null)
            activeTrackers = new ArrayList<>();
        if (tracker == null)
            return;
        pruneCompletedTrackers();
        activeTrackers.add(tracker);
    }

    // drops trackers whose budget has reached 0. natural end state for any
    // spell (it either consumed all volatility or was cancelled to 0).
    public void pruneCompletedTrackers() {
        if (activeTrackers == null || activeTrackers.isEmpty())
            return;
        activeTrackers.removeIf(t -> t == null || t.getRemainingBudget() <= 0f);
    }

    // active staff-pool spell count. slot-bound casts (slotKey != null) are
    // excluded so they don't contend with staff casts for the maxCharges cap.
    // prunes completed trackers first so callers see the live count.
    public int getActiveCount() {
        pruneCompletedTrackers();
        if (activeTrackers == null) return 0;
        int n = 0;
        for (VolatilityTracker t : activeTrackers) {
            if (t != null && t.getSlotKey() == null) n++;
        }
        return n;
    }

    // evicts the oldest staff-pool tracker (slotKey == null). slot-bound
    // trackers are skipped so imbued casts are never evicted by the staff cap.
    public void evictOldest() {
        if (activeTrackers == null || activeTrackers.isEmpty())
            return;
        for (int i = 0; i < activeTrackers.size(); i++) {
            VolatilityTracker t = activeTrackers.get(i);
            if (t != null && t.getSlotKey() == null) {
                activeTrackers.remove(i);
                t.setBudget(0f);
                UUID execId = t.getExecutionId();
                if (execId != null) dependencies.remove(execId);
                return;
            }
        }
    }

    // sets the budget to 0 on any active tracker tagged with the given slot key.
    // used by CastGate to enforce one-cast-per-slot semantics for slot-bound
    // casts (a new Primary fizzles the previous Primary, etc.). pruning happens
    // lazily on the next getActiveCount call.
    public void fizzleSlot(@Nonnull String slotKey) {
        if (activeTrackers == null || activeTrackers.isEmpty()) return;
        pruneCompletedTrackers();
        for (VolatilityTracker t : activeTrackers) {
            if (t == null) continue;
            if (slotKey.equals(t.getSlotKey()) && t.getRemainingBudget() > 0f) {
                t.setBudget(0f);
                UUID execId = t.getExecutionId();
                if (execId != null) dependencies.remove(execId);
            }
        }
    }

    public List<VolatilityTracker> getActiveTrackers() {
        if (activeTrackers == null)
            activeTrackers = new ArrayList<>();
        return activeTrackers;
    }

    public void cancelAll(Ref<EntityStore> casterRef) {
        if (activeTrackers == null || activeTrackers.isEmpty())
            return;
        for (VolatilityTracker tracker : new ArrayList<>(activeTrackers)) {
            if (tracker == null)
                continue;
            if (tracker.getRemainingBudget() <= 0f)
                continue;
            tracker.setBudget(0f);
        }
    }

    public boolean isHoldingPrimary() {
        return holdingPrimary;
    }

    public void setHoldingPrimary(boolean holding) {
        this.holdingPrimary = holding;
    }

    @Nullable
    public Hex getActiveHex() {
        return hex;
    }

    public void setActiveHex(@Nullable Hex hex) {
        this.hex = hex;
    }

    public boolean hasActiveHex() {
        return hex != null;
    }

    public int getCastCount() {
        return castCount;
    }

    public float getCumulativeDecay() {
        return cumulativeDecay;
    }

    public void addDependency(UUID hexId, Ref<EntityStore> dependent) {
        dependencies.computeIfAbsent(hexId, k -> new java.util.ArrayList<>()).add(dependent);
    }

    public Map<UUID, List<Ref<EntityStore>>> getDependencies() {
        return dependencies;
    }

    public List<Ref<EntityStore>> getDependenciesForHex(UUID hexId) {
        return dependencies.getOrDefault(hexId, java.util.Collections.emptyList());
    }

    public List<Ref<EntityStore>> getDependencyList() {
        return dependencies.values().stream().flatMap(List::stream).toList();
    }

    public void advanceCast(float decayRate, float maxVolatility) {
        this.castCount++;
        this.cumulativeDecay += decayRate * maxVolatility;
    }

    public void resetCastState() {
        this.castCount = 0;
        this.cumulativeDecay = 0f;
    }

    public void clear(CommandBuffer<EntityStore> buffer) {

    }

    @Nonnull
    @Override
    public HexcasterIdleComponent clone() {
        HexcasterIdleComponent copy = new HexcasterIdleComponent();
        copy.hex = this.hex != null ? this.hex.clone() : null;
        copy.castCount = this.castCount;
        copy.cumulativeDecay = this.cumulativeDecay;
        copy.holdingPrimary = this.holdingPrimary;
        copy.dependencies = new HashMap<>();
        for (Map.Entry<UUID, List<Ref<EntityStore>>> entry : this.dependencies.entrySet()) {
            copy.dependencies.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        copy.activeTrackers = this.activeTrackers != null ? new ArrayList<>(this.activeTrackers) : new ArrayList<>();
        return copy;
    }
}
