package com.riprod.hexcode.builtin.elementalism.status.block;

import javax.annotation.Nonnull;

import org.joml.Vector3i;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.riprod.hexcode.builtin.elementalism.status.StatusAsset;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;

public class BlockStatusSystem extends EntityTickingSystem<ChunkStore> {
    private final ComponentType<ChunkStore, BlockStatusSection> sectionType;
    private final Query<ChunkStore> query;

    public BlockStatusSystem(ComponentType<ChunkStore, BlockStatusSection> sectionType) {
        this.sectionType = sectionType;
        this.query = Query.and(ChunkSection.getComponentType(), sectionType);
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        var section = archetypeChunk.getComponent(index, this.sectionType);
        var chunkSection = archetypeChunk.getComponent(index, ChunkSection.getComponentType());
        var blocks = archetypeChunk.getComponent(index, BlockSection.getComponentType());
        if (section == null || chunkSection == null || blocks == null) {
            return;
        }
        if (dt > 0f) {
            advance(dt, section, chunkSection, blocks, store);
        }
        if (section.isEmpty()) {
            commandBuffer.tryRemoveComponent(archetypeChunk.getReferenceTo(index), this.sectionType);
        }
    }

    private static void advance(float dt, BlockStatusSection section, ChunkSection chunkSection,
            BlockSection blocks, Store<ChunkStore> store) {
        var world = store.getExternalData().getWorld();
        var entityAccessor = world.getEntityStore().getStore();
        var iterator = Short2ObjectMaps.fastIterator(section.entries());
        while (iterator.hasNext()) {
            var cell = iterator.next();
            var blockIndex = cell.getShortKey();
            var blockId = blocks.get(blockIndex);
            var statuses = cell.getValue().iterator();
            while (statuses.hasNext()) {
                var status = statuses.next();
                if (status.getBlockId() != blockId || !status.advance(dt)) {
                    statuses.remove();
                    continue;
                }
                var asset = StatusAsset.getAssetMap().getAsset(status.getStatusId());
                var effects = asset != null ? asset.getEffects() : null;
                if (effects != null) {
                    var position = new Vector3i(
                            ChunkUtil.minBlock(chunkSection.getX()) + ChunkUtil.xFromIndex(blockIndex),
                            ChunkUtil.minBlock(chunkSection.getY()) + ChunkUtil.yFromIndex(blockIndex),
                            ChunkUtil.minBlock(chunkSection.getZ()) + ChunkUtil.zFromIndex(blockIndex));
                    EffectRenderer.pulse(status.getSustain(), effects, EffectRenderer.blockCenter(position),
                            dt, entityAccessor);
                }
            }
            if (cell.getValue().isEmpty()) {
                iterator.remove();
            }
        }
    }

    @Nonnull
    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
