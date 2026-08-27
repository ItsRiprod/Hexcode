package com.riprod.hexcode.utils;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;

public class BlockAccess {

    public static Ref<ChunkStore> sectionRef(World world, int x, int y, int z) {
        if (y < ChunkUtil.MIN_Y || y >= ChunkUtil.HEIGHT)
            return null;
        Ref<ChunkStore> ref = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        return ref == null || !ref.isValid() ? null : ref;
    }

    public static BlockSection section(World world, int x, int y, int z) {
        Ref<ChunkStore> ref = sectionRef(world, x, y, z);
        return ref == null ? null
                : world.getChunkStore().getStore().getComponent(ref, BlockSection.getComponentType());
    }

    public static int blockId(World world, int x, int y, int z) {
        BlockSection section = section(world, x, y, z);
        return section == null ? BlockType.EMPTY_ID : section.get(x, y, z);
    }

    public static BlockType blockType(World world, int x, int y, int z) {
        int id = blockId(world, x, y, z);
        return id == BlockType.EMPTY_ID ? null : BlockType.getAssetMap().getAsset(id);
    }

    public static int rotationIndex(World world, int x, int y, int z) {
        BlockSection section = section(world, x, y, z);
        return section == null ? RotationTuple.NONE_INDEX : section.getRotationIndex(x, y, z);
    }

    public static Vector3i resolveBase(World world, int x, int y, int z) {
        BlockSection section = section(world, x, y, z);
        if (section == null)
            return null;
        int filler = section.getFiller(x, y, z);
        return new Vector3i(
                x - FillerBlockUtil.unpackX(filler),
                y - FillerBlockUtil.unpackY(filler),
                z - FillerBlockUtil.unpackZ(filler));
    }

    public static boolean setBlock(World world, int x, int y, int z, BlockType blockType,
            int rotationIndex, int settings) {
        Ref<ChunkStore> ref = sectionRef(world, x, y, z);
        if (ref == null)
            return false;
        return BlockOperations.setBlock(world.getChunkStore(), ref, x, y, z,
                BlockType.getAssetMap().getIndex(blockType.getId()), blockType,
                rotationIndex, FillerBlockUtil.NO_FILLER, settings);
    }

    public static boolean clearBlock(World world, int x, int y, int z, int settings) {
        Ref<ChunkStore> ref = sectionRef(world, x, y, z);
        if (ref == null)
            return false;
        return BlockOperations.setBlock(world.getChunkStore(), ref, x, y, z,
                BlockType.EMPTY_ID, BlockType.EMPTY,
                RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER, settings);
    }

    public static Holder<ChunkStore> takeBlockEntity(World world, int x, int y, int z) {
        Ref<ChunkStore> ref = sectionRef(world, x, y, z);
        if (ref == null)
            return null;
        BlockComponentSection components = world.getChunkStore().getStore()
                .getComponent(ref, BlockComponentSection.getComponentType());
        return components == null ? null
                : BlockEntity.takeBlockEntity(world.getChunkStore().getStore(), components, x, y, z);
    }

    public static void putBlockEntity(World world, int x, int y, int z, BlockType blockType,
            int rotationIndex, Holder<ChunkStore> holder) {
        if (holder == null)
            return;
        Ref<ChunkStore> ref = sectionRef(world, x, y, z);
        if (ref == null)
            return;
        Store<ChunkStore> store = world.getChunkStore().getStore();
        BlockComponentSection components = store.getComponent(ref, BlockComponentSection.getComponentType());
        if (components == null)
            return;
        BlockEntity.setBlockEntity(store, ref, components, x, y, z, blockType, rotationIndex, holder);
    }

    public static void setInteractionState(World world, int x, int y, int z, BlockType blockType,
            String state) {
        Ref<ChunkStore> ref = sectionRef(world, x, y, z);
        if (ref == null)
            return;
        BlockOperations.setBlockInteractionState(world.getChunkStore(), ref, x, y, z, blockType, state, false);
    }

    public static int environmentId(World world, int x, int y, int z) {
        Ref<ChunkStore> ref = sectionRef(world, x, y, z);
        if (ref == null)
            return -1;
        Store<ChunkStore> store = world.getChunkStore().getStore();
        ChunkSection section = store.getComponent(ref, ChunkSection.getComponentType());
        if (section == null)
            return -1;
        Ref<ChunkStore> columnRef = section.getChunkColumnReference();
        if (columnRef == null || !columnRef.isValid())
            return -1;
        BlockChunk blockChunk = store.getComponent(columnRef, BlockChunk.getComponentType());
        return blockChunk == null ? -1 : blockChunk.getEnvironment(x, y, z);
    }

    public static class Cursor {
        private final World world;
        private BlockSection section;
        private int sectionX = Integer.MIN_VALUE;
        private int sectionY = Integer.MIN_VALUE;
        private int sectionZ = Integer.MIN_VALUE;

        public Cursor(World world) {
            this.world = world;
        }

        public int blockId(int x, int y, int z) {
            if (y < ChunkUtil.MIN_Y || y >= ChunkUtil.HEIGHT)
                return BlockType.EMPTY_ID;
            int cx = ChunkUtil.chunkCoordinate(x);
            int cy = ChunkUtil.chunkCoordinate(y);
            int cz = ChunkUtil.chunkCoordinate(z);
            if (cx != sectionX || cy != sectionY || cz != sectionZ) {
                sectionX = cx;
                sectionY = cy;
                sectionZ = cz;
                section = BlockAccess.section(world, x, y, z);
            }
            return section == null ? BlockType.EMPTY_ID : section.get(x, y, z);
        }

        public BlockType blockType(int x, int y, int z) {
            int id = blockId(x, y, z);
            return id == BlockType.EMPTY_ID ? null : BlockType.getAssetMap().getAsset(id);
        }
    }
}
