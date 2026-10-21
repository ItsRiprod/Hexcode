package com.riprod.hexcode.utils;

import javax.annotation.Nonnull;

import org.joml.Vector3i;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.EnvironmentSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;

public class BlockAccess {

    public static Ref<ChunkStore> sectionRef(World world, Vector3i pos) {
        if (pos.y < ChunkUtil.MIN_Y)
            return null;
        Ref<ChunkStore> ref = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        return ref == null || !ref.isValid() ? null : ref;
    }

    public static Ref<ChunkStore> sectionRef(ChunkStore store, Vector3i pos) {
        if (pos.y < ChunkUtil.MIN_Y)
            return null;
        var ref = store.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        return ref == null || !ref.isValid() ? null : ref;
    }

    public static BlockSection section(World world, Vector3i pos) {
        var ref = sectionRef(world, pos);
        return ref == null ? null
                : world.getChunkStore().getStore().getComponent(ref, BlockSection.getComponentType());
    }

    public static BlockSection section(Store<ChunkStore> store, Ref<ChunkStore> ref, Vector3i pos) {
        return (ref == null || !ref.isValid()) ? null
                : store.getComponent(ref, BlockSection.getComponentType());
    }

    public static int blockId(World world, Vector3i pos) {
        BlockSection section = section(world, pos);
        return section == null ? BlockType.EMPTY_ID : section.get(pos.x, pos.y, pos.z);
    }

    public static BlockType blockType(World world, Vector3i pos) {
        int id = blockId(world, pos);
        return id == BlockType.EMPTY_ID ? null : BlockType.getAssetMap().getAsset(id);
    }

    public static int rotationIndex(World world, Vector3i pos) {
        BlockSection section = section(world, pos);
        return section == null ? RotationTuple.NONE_INDEX : section.getRotationIndex(pos.x, pos.y, pos.z);
    }

    public static Vector3i resolveBase(World world, Vector3i pos) {
        BlockSection section = section(world, pos);
        if (section == null)
            return null;
        int filler = section.getFiller(pos.x, pos.y, pos.z);
        return new Vector3i(
                pos.x - FillerBlockUtil.unpackX(filler),
                pos.y - FillerBlockUtil.unpackY(filler),
                pos.z - FillerBlockUtil.unpackZ(filler));
    }

    public static boolean setBlock(World world, Vector3i pos, BlockType blockType,
            int rotationIndex, int settings) {
        Ref<ChunkStore> ref = sectionRef(world, pos);
        if (ref == null)
            return false;
        return BlockOperations.setBlock(world.getChunkStore(), ref, pos.x, pos.y, pos.z,
                BlockType.getAssetMap().getIndex(blockType.getId()), blockType,
                rotationIndex, FillerBlockUtil.NO_FILLER, settings);
    }

    public static boolean setBlock(Cursor cursor, Vector3i pos, BlockType blockType,
            int rotationIndex, int settings) {
        Ref<ChunkStore> ref = cursor.sectionRef(pos);
        if (ref == null)
            return false;
        return BlockOperations.setBlock(cursor.chunkStore(), ref, pos.x, pos.y, pos.z,
                BlockType.getAssetMap().getIndex(blockType.getId()), blockType,
                rotationIndex, FillerBlockUtil.NO_FILLER, settings);
    }

    public static boolean clearBlock(World world, Vector3i pos, int settings) {
        Ref<ChunkStore> ref = sectionRef(world, pos);
        if (ref == null)
            return false;
        return BlockOperations.setBlock(world.getChunkStore(), ref, pos.x, pos.y, pos.z,
                BlockType.EMPTY_ID, BlockType.EMPTY,
                RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER, settings);
    }

    public static Holder<ChunkStore> takeBlockEntity(World world, Vector3i pos) {
        Ref<ChunkStore> ref = sectionRef(world, pos);
        if (ref == null)
            return null;
        BlockComponentSection components = world.getChunkStore().getStore()
                .getComponent(ref, BlockComponentSection.getComponentType());
        return components == null ? null
                : BlockEntity.takeBlockEntity(world.getChunkStore().getStore(), components, pos.x, pos.y, pos.z);
    }

    public static void putBlockEntity(World world, Vector3i pos, BlockType blockType,
            int rotationIndex, Holder<ChunkStore> holder) {
        if (holder == null)
            return;
        Ref<ChunkStore> ref = sectionRef(world, pos);
        if (ref == null)
            return;
        Store<ChunkStore> store = world.getChunkStore().getStore();
        BlockComponentSection components = store.getComponent(ref, BlockComponentSection.getComponentType());
        if (components == null)
            return;
        BlockEntity.setBlockEntity(store, ref, components, pos.x, pos.y, pos.z, blockType, rotationIndex, holder);
    }

    public static void setInteractionState(World world, Vector3i pos, BlockType blockType,
            String state) {
        Ref<ChunkStore> ref = sectionRef(world, pos);
        if (ref == null)
            return;
        BlockOperations.setBlockInteractionState(world.getChunkStore(), ref, pos.x, pos.y, pos.z, blockType, state,
                false);
    }

    public static int environmentId(World world, Vector3i pos) {
        Ref<ChunkStore> ref = sectionRef(world, pos);
        if (ref == null)
            return -1;
        var section = world.getChunkStore().getStore().getComponent(ref, EnvironmentSection.getComponentType());
        return section == null ? -1 : section.get(pos.x, pos.y, pos.z);
    }

    public static class Cursor {
        @Nonnull 
        private final World world;
        @Nonnull 
        private final ChunkStore store;
        private BlockSection section;
        private int sectionX = Integer.MIN_VALUE;
        private int sectionY = Integer.MIN_VALUE;
        private int sectionZ = Integer.MIN_VALUE;
        private Ref<ChunkStore> ref;

        public Cursor(@Nonnull World world) {
            this.world = world;
            store = world.getChunkStore();
        }

        /** Sets the cursor to a different coordinate */
        public void set(Vector3i pos) {
            int cx = ChunkUtil.chunkCoordinate(pos.x);
            int cy = ChunkUtil.chunkCoordinate(pos.y);
            int cz = ChunkUtil.chunkCoordinate(pos.z);

            // if outside of current section, get the new section
            if (cx != sectionX || cy != sectionY || cz != sectionZ) {
                sectionX = cx;
                sectionY = cy;
                sectionZ = cz;
                ref = BlockAccess.sectionRef(store, pos);
                section = BlockAccess.section(store.getStore(), ref, pos);
            }
        }

        public int blockId(Vector3i pos) {
            if (pos.y < ChunkUtil.MIN_Y)
                return BlockType.EMPTY_ID;
            set(pos);
            return section == null ? BlockType.EMPTY_ID : section.get(pos.x, pos.y, pos.z);
        }

        public BlockType blockType(Vector3i pos) {
            int id = blockId(pos);
            return id == BlockType.EMPTY_ID ? null : BlockType.getAssetMap().getAsset(id);
        }

        public BlockSection section(Vector3i pos) {
            set(pos);
            return section;
        }

        public Ref<ChunkStore> sectionRef(Vector3i pos) {
            set(pos);
            return ref;
        }

        public ChunkStore chunkStore() {
            return store;
        }

        public World world() {
            return world;
        }
    }
}
