package com.riprod.hexcode.utils;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.SectionReader;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;

public class BlockUtils {

    public static Box resolveBlockDisplayBox(World world, Vector3i blockPos) {
        BlockSection blockSection = BlockAccess.section(world, blockPos);
        if (blockSection == null) {
            return null;
        }

        var rotation = blockSection.getRotationIndex(blockPos.x, blockPos.y, blockPos.z);

        BlockType blockType = BlockType.getAssetMap()
                .getAsset(blockSection.get(blockPos.x, blockPos.y, blockPos.z));
        if (blockType == null)
            return null;
        var hitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
        if (hitbox == null)
            return null;
        return hitbox.get(rotation).getBoundingBox().clone();
    }

    private record BlockMove(Vector3i base, Vector3i dest,
            BlockType blockType, int rotationIndex) {
    }

    private static BlockMove planMove(World world, Vector3i source, Vector3d destination) {
        if (source.y() < ChunkUtil.MIN_Y)
            return null;

        int blockId = BlockAccess.blockId(world, source);
        if (blockId == BlockType.EMPTY_ID)
            return null;
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null)
            return null;

        BlockSection sourceSection = BlockAccess.section(world, source);
        if (sourceSection == null)
            return null;

        int filler = sourceSection.getFiller(source.x(), source.y(), source.z());
        source.set(
                source.x() - FillerBlockUtil.unpackX(filler),
                source.y() - FillerBlockUtil.unpackY(filler),
                source.z() - FillerBlockUtil.unpackZ(filler));

        BlockSection baseSection = BlockAccess.section(world, source);
        if (baseSection == null || baseSection.get(source.x, source.y, source.z) != blockId)
            return null;
        int rotationIndex = baseSection.getRotationIndex(source.x, source.y, source.z);

        var dest = Vector3dUtil.toVector3i(destination);
        var isAir = isAir(world, dest);
        if (isAir)
            return null;

        return new BlockMove(source, dest,
                blockType, rotationIndex);
    }

    private static void applyMove(World world, BlockMove move) {
        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> store = chunkStore.getStore();

        Ref<ChunkStore> srcRef = chunkStore.getChunkSectionReferenceAtBlock(move.base().x(), move.base().y(),
                move.base().z());
        Ref<ChunkStore> dstRef = chunkStore.getChunkSectionReferenceAtBlock(move.dest.x(), move.dest.y(),
                move.dest.z());
        if (srcRef == null || !srcRef.isValid() || dstRef == null || !dstRef.isValid())
            return;

        BlockComponentSection srcComponents = store.getComponent(srcRef, BlockComponentSection.getComponentType());
        BlockComponentSection dstComponents = store.getComponent(dstRef, BlockComponentSection.getComponentType());

        Holder<ChunkStore> holder = srcComponents == null ? null
                : BlockEntity.takeBlockEntity(store, srcComponents, move.base().x, move.base().y(), move.base().z());

        boolean cleared = BlockOperations.setBlock(chunkStore, srcRef,
                move.base.x(), move.base.y(), move.base.z(),
                BlockType.EMPTY_ID, BlockType.EMPTY,
                RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE);

        if (!cleared) {
            if (holder != null && srcComponents != null) {
                BlockEntity.setBlockEntity(store, srcRef, srcComponents,
                        move.base.x(), move.base.y(), move.base.z(),
                        move.blockType(), move.rotationIndex(), holder);
            }
            return;
        }

        BlockOperations.setBlock(chunkStore, dstRef,
                move.dest.x(), move.dest.y(), move.dest.z(),
                BlockType.getAssetMap().getIndex(move.blockType().getId()), move.blockType(),
                move.rotationIndex(), FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE
                        | SetBlockSettings.PERFORM_BLOCK_UPDATE);

        if (holder != null && dstComponents != null) {
            BlockEntity.setBlockEntity(store, dstRef, dstComponents,
                    move.dest.x(), move.dest.y(), move.dest.z(),
                    move.blockType(), move.rotationIndex(), holder);
        }

        markDecoAt(world, move.dest);
    }

    public static boolean rotateBlock(Vector3i target, Rotation3f rotation, World world,
            CommandBuffer<EntityStore> accessor, Ref<EntityStore> caster, String glyphName) {
        if (target.y() < ChunkUtil.MIN_Y)
            return false;

        int blockId = BlockAccess.blockId(world, target);
        if (blockId == BlockType.EMPTY_ID)
            return false;
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null)
            return false;

        BlockSection hitSection = BlockAccess.section(world, target);
        if (hitSection == null)
            return false;

        int filler = hitSection.getFiller(target.x(), target.y(), target.z());
        var base = new Vector3i(
                target.x() - FillerBlockUtil.unpackX(filler),
                target.y() - FillerBlockUtil.unpackY(filler),
                target.z() - FillerBlockUtil.unpackZ(filler));
        if (base.y < ChunkUtil.MIN_Y)
            return false;

        var baseSection = BlockAccess.section(world, base);
        if (baseSection == null || baseSection.get(base.x, base.y, base.z) != blockId)
            return false;

        var chunkStore = world.getChunkStore();
        var store = chunkStore.getStore();
        var baseRef = chunkStore.getChunkSectionReferenceAtBlock(base.x, base.y, base.z);
        if (baseRef == null || !baseRef.isValid())
            return false;

        int oldRotation = baseSection.getRotationIndex(base.x, base.y, base.z);
        RotationTuple newRotation = RotationTuple.of(
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.yaw())),
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.pitch())),
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.roll())));
        if (newRotation.index() == oldRotation)
            return true;

        boolean fits = BlockOperations.testPlaceBlock(store, baseSection, base.x, base.y, base.z, blockType,
                newRotation.index(),
                (cx, cy, cz, cellType, cellRotation, cellFiller) -> cx - FillerBlockUtil.unpackX(cellFiller) == base.x
                        && cy - FillerBlockUtil.unpackY(cellFiller) == base.y
                        && cz - FillerBlockUtil.unpackZ(cellFiller) == base.z);
        if (!fits)
            return false;

        if (!HexProtection.canModifyBlock(world, caster, accessor, base, BlockAction.BREAK)
                || !HexProtection.canModifyBlock(world, caster, accessor, base, BlockAction.PLACE)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return false;
        }

        BlockComponentSection baseComponents = store.getComponent(baseRef, BlockComponentSection.getComponentType());

        Holder<ChunkStore> holder = baseComponents == null ? null
                : BlockEntity.takeBlockEntity(store, baseComponents, base.x, base.y, base.z);

        boolean cleared = BlockOperations.setBlock(chunkStore, baseRef, base.x, base.y, base.z,
                BlockType.EMPTY_ID, BlockType.EMPTY,
                RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE);

        if (!cleared) {
            if (holder != null && baseComponents != null) {
                BlockEntity.setBlockEntity(store, baseRef, baseComponents, base.x, base.y, base.z,
                        blockType, oldRotation, holder);
            }
            return false;
        }

        BlockOperations.setBlock(chunkStore, baseRef, base.x, base.y, base.z,
                BlockType.getAssetMap().getIndex(blockType.getId()), blockType,
                newRotation.index(), FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE
                        | SetBlockSettings.PERFORM_BLOCK_UPDATE);

        if (holder != null && baseComponents != null) {
            BlockEntity.setBlockEntity(store, baseRef, baseComponents, base.x, base.y, base.z,
                    blockType, newRotation.index(), holder);
        }
        markDecoAt(world, base);
        return true;
    }

    private static void markDecoAt(World world, Vector3i pos) {
        if (pos.y < ChunkUtil.MIN_Y)
            return;
        Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (sectionRef == null || !sectionRef.isValid())
            return;
        BlockPhysics.markDeco(world.getChunkStore().getStore(), sectionRef, pos.x, pos.y, pos.z);
    }

    private static boolean isMultiCell(World world, Vector3i pos) {
        if (pos.y < ChunkUtil.MIN_Y)
            return false;
        BlockSection section = BlockAccess.section(world, pos);
        if (section == null)
            return false;
        return FillerBlockUtil.multiCellFootprint(section.get(pos.x, pos.y, pos.z),
                section.getRotationIndex(pos.x, pos.y, pos.z)) != null;
    }

    public static boolean isAir(World world, Vector3i pos) {

        var section = BlockAccess.section(world, pos);
        return section == null ? true : section.get(pos.x, pos.y, pos.z) == BlockType.EMPTY_ID;
    }

    public static void moveToDestination(HexVar var, Vector3d dest, World world, HexContext hexContext,
            String glyphName) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (var instanceof EntityVar entityVar) {
            Ref<EntityStore> entityRef = entityVar.getRef(accessor);
            if (entityRef == null || !entityRef.isValid())
                return;

            if (!HexProtection.canAffectEntity(world, caster, accessor, entityRef)) {
                HexProtection.notifyBlocked(caster, accessor, glyphName);
                return;
            }

            if (accessor.getComponent(entityRef, TransformComponent.getComponentType()) == null)
                return;

            accessor.putComponent(entityRef, Teleport.getComponentType(),
                    new Teleport(dest, Rotation3f.NaN));
        } else if (var instanceof BlockVar blockVar && blockVar.getValue() != null) {
            moveBlockGated(blockVar.getValue(), dest, world, accessor, caster, glyphName);
        } else if (var instanceof PositionVar posVar && posVar.getValue() != null) {
            Vector3i sourceBlock = new Vector3i(
                    (int) Math.floor(posVar.getValue().x()),
                    (int) Math.floor(posVar.getValue().y()),
                    (int) Math.floor(posVar.getValue().z()));
            moveBlockGated(sourceBlock, dest, world, accessor, caster, glyphName);
        }
    }

    private static void moveBlockGated(Vector3i source, Vector3d dest, World world,
            CommandBuffer<EntityStore> accessor, Ref<EntityStore> caster, String glyphName) {
        BlockMove move = planMove(world, source, dest);
        if (move == null)
            return;

        if (!HexProtection.canModifyBlock(world, caster, accessor, move.base(), BlockAction.BREAK)
            || !HexProtection.canModifyBlock(world, caster, accessor, move.dest(), BlockAction.PLACE)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }
        applyMove(world, move);
    }

    public static void swapPair(HexVar a, HexVar b, World world, HexContext hexContext, String glyphName) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        var posA = a.position(accessor);
        var posB = b.position(accessor);
        if (posA == null || posB == null)
            return;

        if (a instanceof EntityVar || b instanceof EntityVar) {
            moveToDestination(a, posB, world, hexContext, glyphName);
            moveToDestination(b, posA, world, hexContext, glyphName);
            return;
        }

        var blockA = Vector3dUtil.toVector3i(posA);
        var blockB = Vector3dUtil.toVector3i(posB);

        if (!HexProtection.canModifyBlock(world, caster, accessor, blockA, BlockAction.BREAK)
                || !HexProtection.canModifyBlock(world, caster, accessor, blockB,
                        BlockAction.BREAK)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }

        var reader = new SectionReader(world.getChunkStore());
        var cursorA = reader.sectionCursor();
        var cursorB = reader.sectionCursor();
        if (!cursorA.moveTo(blockA.x, blockA.y, blockA.z) || !cursorB.moveTo(blockB.x, blockB.y, blockB.z))
            return;

        if (isMultiCell(world, blockA) || isMultiCell(world, blockB)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }

        var selA = new BlockSelection();
        selA.setPosition(blockA.x, blockA.y, blockA.z);
        cursorA.copyInto(selA);

        var selB = new BlockSelection();
        selB.setPosition(blockB.x, blockB.y, blockB.z);
        cursorB.copyInto(selB);

        selA.setPosition(blockB.x, blockB.y, blockB.z);
        selB.setPosition(blockA.x, blockA.y, blockA.z);
        selA.placeNoReturn(world, new Vector3i(), accessor);
        selB.placeNoReturn(world, new Vector3i(), accessor);
    }
}
