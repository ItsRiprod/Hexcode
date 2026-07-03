package com.riprod.hexcode.core.common.pedestal.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;

public class PedestalPlaceEvent extends RefSystem<ChunkStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nullable
    @Override
    public Query<ChunkStore> getQuery() {
        return PedestalBlockComponent.getComponentType();
    }

    @Override
    public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason,
            @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {

        PedestalBlockComponent pedestal = commandBuffer.getComponent(ref, PedestalBlockComponent.getComponentType());
        BlockModule.BlockStateInfo blockStateInfo = commandBuffer.getComponent(ref,
                BlockModule.BlockStateInfo.getComponentType());
        if (pedestal == null || blockStateInfo == null) {
            return;
        }

        Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
        if (!chunkRef.isValid()) {
            return;
        }

        BlockChunk blockChunk = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunk == null) {
            return;
        }

        int blockIndex = blockStateInfo.getIndex();
        int localX = ChunkUtil.xFromBlockInColumn(blockIndex);
        int localY = ChunkUtil.yFromBlockInColumn(blockIndex);
        int localZ = ChunkUtil.zFromBlockInColumn(blockIndex);
        int blockX = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), localX);
        int blockZ = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), localZ);
        Vector3i blockPos = new Vector3i(blockX, localY, blockZ);

        pedestal.setLocation(blockPos);

        World world = commandBuffer.getExternalData().getWorld();
        Store<EntityStore> entityStore = world.getEntityStore().getStore();
        Holder<EntityStore> anchorHolder = PedestalEntity.buildAnchorHolder(entityStore, blockPos);
        if (anchorHolder == null) {
            return;
        }

        world.execute(() -> {
            Ref<EntityStore> anchorRef = entityStore.addEntity(anchorHolder, AddReason.SPAWN);
            PedestalBlockComponent ped = BlockModule.getComponent(
                    PedestalBlockComponent.getComponentType(), world,
                    blockPos.x, blockPos.y, blockPos.z);
            if (ped != null) {
                ped.setAnchorRef(anchorRef);
            }
        });
    }

    @Override
    public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason,
            @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    }
}
