package com.riprod.hexcode.builtin.elementalism.status.block;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class BlockStatusSection implements Component<ChunkStore> {
    private static ComponentType<ChunkStore, BlockStatusSection> componentType;

    private final Short2ObjectOpenHashMap<List<BlockStatusEntry>> entries = new Short2ObjectOpenHashMap<>();

    public static void setComponentType(ComponentType<ChunkStore, BlockStatusSection> type) {
        componentType = type;
    }

    public static ComponentType<ChunkStore, BlockStatusSection> getComponentType() {
        return componentType;
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Nullable
    public List<BlockStatusEntry> get(int index) {
        return this.entries.get((short) index);
    }

    public List<BlockStatusEntry> getOrCreate(int index) {
        return this.entries.computeIfAbsent((short) index, k -> new ArrayList<>(2));
    }

    public void prune(int index, int blockId) {
        var list = get(index);
        if (list == null) {
            return;
        }
        list.removeIf(entry -> entry.getBlockId() != blockId);
        if (list.isEmpty()) {
            this.entries.remove((short) index);
        }
    }

    public boolean remove(int index, BlockStatusEntry entry) {
        var list = get(index);
        if (list == null || !list.remove(entry)) {
            return false;
        }
        if (list.isEmpty()) {
            this.entries.remove((short) index);
        }
        return true;
    }

    Short2ObjectMap<List<BlockStatusEntry>> entries() {
        return this.entries;
    }

    @Nonnull
    @Override
    public BlockStatusSection clone() {
        var copy = new BlockStatusSection();
        for (var entry : this.entries.short2ObjectEntrySet()) {
            var list = new ArrayList<BlockStatusEntry>(entry.getValue().size());
            for (var status : entry.getValue()) {
                list.add(status.copy());
            }
            copy.entries.put(entry.getShortKey(), list);
        }
        return copy;
    }
}
