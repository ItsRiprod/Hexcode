package com.riprod.hexcode.core.common.execution.cast;

import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public final class HexCast {

    public static final CastComponentRegistry REGISTRY = new CastComponentRegistry();

    private static final CastComponent[] EMPTY = new CastComponent[0];

    private UUID executionId = UUID.randomUUID();
    private final LongOpenHashSet activeBranches = new LongOpenHashSet();
    private long branchIdSeq = 0L;
    @Nullable
    private String slotKey;
    private boolean fizzleNotified;
    private CastComponent[] components = EMPTY;

    public HexCast() {
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CastComponent> T get(@Nonnull CastComponentType<T> type) {
        int index = type.getIndex();
        return index < components.length ? (T) components[index] : null;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <T extends CastComponent> T getOrCreate(@Nonnull CastComponentType<T> type) {
        int index = type.getIndex();
        if (index >= components.length) {
            components = Arrays.copyOf(components, index + 1);
        }
        CastComponent existing = components[index];
        if (existing == null) {
            existing = type.create();
            components[index] = existing;
        }
        return (T) existing;
    }

    public <T extends CastComponent> void put(@Nonnull CastComponentType<T> type, @Nonnull T component) {
        int index = type.getIndex();
        if (index >= components.length) {
            components = Arrays.copyOf(components, index + 1);
        }
        components[index] = component;
    }

    @Nonnull
    public VolatilityComponent volatility() {
        return getOrCreate(VolatilityComponent.getComponentType());
    }

    @Nullable
    public ResourcePoolComponent resources() {
        return get(ResourcePoolComponent.getComponentType());
    }

    @Nonnull
    public ResourcePoolComponent mutableResources() {
        return getOrCreate(ResourcePoolComponent.getComponentType());
    }

    public UUID getExecutionId() {
        return executionId;
    }

    @Nullable
    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(@Nullable String slotKey) {
        this.slotKey = slotKey;
    }

    public long openBranch() {
        long id = ++branchIdSeq;
        activeBranches.add(id);
        return id;
    }

    public void closeBranch(long id) {
        activeBranches.remove(id);
    }

    public int getActiveBranchCount() {
        return activeBranches.size();
    }

    public boolean claimFizzleNotice() {
        if (fizzleNotified) {
            return false;
        }
        fizzleNotified = true;
        return true;
    }

    @Nonnull
    public HexCast copy() {
        HexCast copy = new HexCast();
        copy.executionId = this.executionId;
        copy.slotKey = this.slotKey;
        copy.fizzleNotified = this.fizzleNotified;
        copy.components = new CastComponent[this.components.length];
        for (int i = 0; i < this.components.length; i++) {
            CastComponent component = this.components[i];
            copy.components[i] = component != null ? component.copy() : null;
        }
        return copy;
    }
}
