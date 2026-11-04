package com.riprod.hexcode.core.common.execution.cast;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;
import com.riprod.hexcode.core.common.execution.context.HexContext;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

public final class HexCast {

    private UUID executionId = UUID.randomUUID();
    private final Ref<EntityStore> rootRef;
    private final Long2ObjectLinkedOpenHashMap<HexContext> liveBranches = new Long2ObjectLinkedOpenHashMap<>();
    private long branchIdSeq = 0L;
    private boolean fizzleNotified;
    private boolean ended;
    private final CastComponent[] components;

    public HexCast(@Nonnull Ref<EntityStore> rootRef, @Nonnull CastComponent[] components) {
        this.rootRef = rootRef;
        this.components = components;
    }

    @Nonnull
    public Ref<EntityStore> getRootRef() {
        return rootRef;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CastComponent> T get(@Nonnull CastComponentType<T> type) {
        return (T) components[type.getIndex()];
    }

    public <T extends CastComponent> void put(@Nonnull CastComponentType<T> type, @Nonnull T component) {
        var index = type.getIndex();
        if (components[index] != null) {
            throw new IllegalStateException("cast component " + type.getId()
                    + " is already set; mutate the existing instance");
        }
        components[index] = component;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public long openBranch(@Nonnull HexContext branch) {
        var id = ++branchIdSeq;
        if (!ended) {
            liveBranches.put(id, branch);
        }
        return id;
    }

    public boolean closeBranch(long id) {
        return liveBranches.remove(id) != null;
    }

    public boolean isBranchLive(long id) {
        return liveBranches.containsKey(id);
    }

    public int getActiveBranchCount() {
        return liveBranches.size();
    }

    @Nullable
    public HexContext firstLiveBranch() {
        return liveBranches.isEmpty() ? null : liveBranches.get(liveBranches.firstLongKey());
    }

    public boolean markEnded() {
        if (ended) {
            return false;
        }
        ended = true;
        return true;
    }

    public boolean isEnded() {
        return ended;
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
        var copiedComponents = new CastComponent[components.length];
        for (int i = 0; i < components.length; i++) {
            var component = components[i];
            copiedComponents[i] = component != null ? component.copy() : null;
        }
        var copy = new HexCast(rootRef, copiedComponents);
        copy.executionId = this.executionId;
        copy.fizzleNotified = this.fizzleNotified;
        copy.ended = this.ended;
        return copy;
    }
}
