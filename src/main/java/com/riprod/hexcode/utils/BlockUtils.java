package com.riprod.hexcode.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
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

    private static final int DEFAULT_SEARCH_RADIUS = 5;

    public static Box resolveBlockDisplayBox(World world, Vector3i blockPos) {
        BlockSection blockSection = BlockAccess.section(world, blockPos.x, blockPos.y, blockPos.z);
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

    private record BlockMove(int baseX, int baseY, int baseZ, int destX, int destY, int destZ,
            BlockType blockType, int rotationIndex) {
    }

    private static BlockMove planMove(World world, Vector3i source, Vector3d destination) {
        if (source.y() < ChunkUtil.MIN_Y || source.y() >= ChunkUtil.HEIGHT)
            return null;

        int blockId = BlockAccess.blockId(world, source.x(), source.y(), source.z());
        if (blockId == BlockType.EMPTY_ID)
            return null;
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null)
            return null;

        BlockSection sourceSection = BlockAccess.section(world, source.x(), source.y(), source.z());
        if (sourceSection == null)
            return null;

        int filler = sourceSection.getFiller(source.x(), source.y(), source.z());
        int baseX = source.x() - FillerBlockUtil.unpackX(filler);
        int baseY = source.y() - FillerBlockUtil.unpackY(filler);
        int baseZ = source.z() - FillerBlockUtil.unpackZ(filler);
        if (baseY < ChunkUtil.MIN_Y || baseY >= ChunkUtil.HEIGHT)
            return null;

        BlockSection baseSection = BlockAccess.section(world, baseX, baseY, baseZ);
        if (baseSection == null || baseSection.get(baseX, baseY, baseZ) != blockId)
            return null;
        int rotationIndex = baseSection.getRotationIndex(baseX, baseY, baseZ);

        Store<ChunkStore> store = world.getChunkStore().getStore();
        Vector3i placement = findAirBlock(world,
                (int) Math.floor(destination.x()),
                (int) Math.floor(destination.y()),
                (int) Math.floor(destination.z()),
                DEFAULT_SEARCH_RADIUS,
                (cx, cy, cz) -> {
                    BlockSection candidateSection = BlockAccess.section(world, cx, cy, cz);
                    return candidateSection != null
                            && BlockOperations.testPlaceBlock(store, candidateSection, cx, cy, cz, blockType,
                                    rotationIndex,
                                    (fx, fy, fz, cellType, cellRotation,
                                            cellFiller) -> fx - FillerBlockUtil.unpackX(cellFiller) == baseX
                                                    && fy - FillerBlockUtil.unpackY(cellFiller) == baseY
                                                    && fz - FillerBlockUtil.unpackZ(cellFiller) == baseZ);
                });
        if (placement == null)
            return null;

        return new BlockMove(baseX, baseY, baseZ, placement.x(), placement.y(), placement.z(),
                blockType, rotationIndex);
    }

    private static void applyMove(World world, BlockMove move) {
        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> store = chunkStore.getStore();

        Ref<ChunkStore> srcRef = chunkStore.getChunkSectionReferenceAtBlock(move.baseX(), move.baseY(), move.baseZ());
        Ref<ChunkStore> dstRef = chunkStore.getChunkSectionReferenceAtBlock(move.destX(), move.destY(), move.destZ());
        if (srcRef == null || !srcRef.isValid() || dstRef == null || !dstRef.isValid())
            return;

        BlockComponentSection srcComponents = store.getComponent(srcRef, BlockComponentSection.getComponentType());
        BlockComponentSection dstComponents = store.getComponent(dstRef, BlockComponentSection.getComponentType());

        Holder<ChunkStore> holder = srcComponents == null ? null
                : BlockEntity.takeBlockEntity(store, srcComponents, move.baseX(), move.baseY(), move.baseZ());

        boolean cleared = BlockOperations.setBlock(chunkStore, srcRef,
                move.baseX(), move.baseY(), move.baseZ(),
                BlockType.EMPTY_ID, BlockType.EMPTY,
                RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE);

        if (!cleared) {
            if (holder != null && srcComponents != null) {
                BlockEntity.setBlockEntity(store, srcRef, srcComponents,
                        move.baseX(), move.baseY(), move.baseZ(),
                        move.blockType(), move.rotationIndex(), holder);
            }
            return;
        }

        BlockOperations.setBlock(chunkStore, dstRef,
                move.destX(), move.destY(), move.destZ(),
                BlockType.getAssetMap().getIndex(move.blockType().getId()), move.blockType(),
                move.rotationIndex(), FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE
                        | SetBlockSettings.PERFORM_BLOCK_UPDATE);

        if (holder != null && dstComponents != null) {
            BlockEntity.setBlockEntity(store, dstRef, dstComponents,
                    move.destX(), move.destY(), move.destZ(),
                    move.blockType(), move.rotationIndex(), holder);
        }

        markDecoAt(world, move.destX(), move.destY(), move.destZ());
    }

    public static boolean rotateBlock(Vector3i target, Rotation3f rotation, World world,
            CommandBuffer<EntityStore> accessor, Ref<EntityStore> caster, String glyphName) {
        if (target.y() < ChunkUtil.MIN_Y || target.y() >= ChunkUtil.HEIGHT)
            return false;

        int blockId = BlockAccess.blockId(world, target.x(), target.y(), target.z());
        if (blockId == BlockType.EMPTY_ID)
            return false;
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null)
            return false;

        BlockSection hitSection = BlockAccess.section(world, target.x(), target.y(), target.z());
        if (hitSection == null)
            return false;

        int filler = hitSection.getFiller(target.x(), target.y(), target.z());
        int baseX = target.x() - FillerBlockUtil.unpackX(filler);
        int baseY = target.y() - FillerBlockUtil.unpackY(filler);
        int baseZ = target.z() - FillerBlockUtil.unpackZ(filler);
        if (baseY < ChunkUtil.MIN_Y || baseY >= ChunkUtil.HEIGHT)
            return false;

        BlockSection baseSection = BlockAccess.section(world, baseX, baseY, baseZ);
        if (baseSection == null || baseSection.get(baseX, baseY, baseZ) != blockId)
            return false;

        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> store = chunkStore.getStore();
        Ref<ChunkStore> baseRef = chunkStore.getChunkSectionReferenceAtBlock(baseX, baseY, baseZ);
        if (baseRef == null || !baseRef.isValid())
            return false;

        int oldRotation = baseSection.getRotationIndex(baseX, baseY, baseZ);
        RotationTuple newRotation = RotationTuple.of(
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.yaw())),
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.pitch())),
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.roll())));
        if (newRotation.index() == oldRotation)
            return true;

        boolean fits = BlockOperations.testPlaceBlock(store, baseSection, baseX, baseY, baseZ, blockType,
                newRotation.index(),
                (cx, cy, cz, cellType, cellRotation, cellFiller) -> cx - FillerBlockUtil.unpackX(cellFiller) == baseX
                        && cy - FillerBlockUtil.unpackY(cellFiller) == baseY
                        && cz - FillerBlockUtil.unpackZ(cellFiller) == baseZ);
        if (!fits)
            return false;

        Vector3i base = new Vector3i(baseX, baseY, baseZ);
        if (!HexProtection.canModifyBlock(world, caster, accessor, new Vector3i(base), BlockAction.BREAK)
                || !HexProtection.canModifyBlock(world, caster, accessor, new Vector3i(base), BlockAction.PLACE)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return false;
        }

        BlockComponentSection baseComponents = store.getComponent(baseRef, BlockComponentSection.getComponentType());

        Holder<ChunkStore> holder = baseComponents == null ? null
                : BlockEntity.takeBlockEntity(store, baseComponents, baseX, baseY, baseZ);

        boolean cleared = BlockOperations.setBlock(chunkStore, baseRef, baseX, baseY, baseZ,
                BlockType.EMPTY_ID, BlockType.EMPTY,
                RotationTuple.NONE_INDEX, FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE);

        if (!cleared) {
            if (holder != null && baseComponents != null) {
                BlockEntity.setBlockEntity(store, baseRef, baseComponents, baseX, baseY, baseZ,
                        blockType, oldRotation, holder);
            }
            return false;
        }

        BlockOperations.setBlock(chunkStore, baseRef, baseX, baseY, baseZ,
                BlockType.getAssetMap().getIndex(blockType.getId()), blockType,
                newRotation.index(), FillerBlockUtil.NO_FILLER,
                SetBlockSettings.NO_SEND_PARTICLES | SetBlockSettings.NO_UPDATE_STATE
                        | SetBlockSettings.PERFORM_BLOCK_UPDATE);

        if (holder != null && baseComponents != null) {
            BlockEntity.setBlockEntity(store, baseRef, baseComponents, baseX, baseY, baseZ,
                    blockType, newRotation.index(), holder);
        }
        markDecoAt(world, baseX, baseY, baseZ);
        return true;
    }

    private static void markDecoAt(World world, int x, int y, int z) {
        if (y < ChunkUtil.MIN_Y || y >= ChunkUtil.HEIGHT)
            return;
        Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid())
            return;
        BlockPhysics.markDeco(world.getChunkStore().getStore(), sectionRef, x, y, z);
    }

    private static boolean isMultiCell(World world, int x, int y, int z) {
        if (y < ChunkUtil.MIN_Y || y >= ChunkUtil.HEIGHT)
            return false;
        BlockSection section = BlockAccess.section(world, x, y, z);
        if (section == null)
            return false;
        return FillerBlockUtil.multiCellFootprint(section.get(x, y, z), section.getRotationIndex(x, y, z)) != null;
    }

    public static Vector3i findAirBlock(World world, int x, int y, int z, int maxSearchRadius,
            TriIntPredicate accepts) {
        BlockAccess.Cursor cursor = new BlockAccess.Cursor(world);
        if (cursor.blockId(x, y, z) == BlockType.EMPTY_ID && (accepts == null || accepts.test(x, y, z)))
            return new Vector3i(x, y, z);

        for (int dist = 1; dist <= maxSearchRadius; dist++) {
            for (int dy = dist; dy >= -dist; dy--) {
                for (int dx = -(dist - Math.abs(dy)); dx <= dist - Math.abs(dy); dx++) {
                    int dz = dist - Math.abs(dy) - Math.abs(dx);
                    for (int sz = (dz == 0 ? 0 : -1); sz <= 1; sz += 2) {
                        int cx = x + dx;
                        int cy = y + dy;
                        int cz = z + (dz * sz);
                        if (cy < ChunkUtil.MIN_Y || cy >= ChunkUtil.HEIGHT)
                            continue;
                        if (cursor.blockId(cx, cy, cz) == BlockType.EMPTY_ID
                                && (accepts == null || accepts.test(cx, cy, cz))) {
                            return new Vector3i(cx, cy, cz);
                        }
                        if (dz == 0)
                            break;
                    }
                }
            }
        }
        return null;
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

        if (!HexProtection.canModifyBlock(world, caster, accessor,
                new Vector3i(move.baseX(), move.baseY(), move.baseZ()), BlockAction.BREAK)
                || !HexProtection.canModifyBlock(world, caster, accessor,
                        new Vector3i(move.destX(), move.destY(), move.destZ()), BlockAction.PLACE)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }
        applyMove(world, move);
    }

    public static void swapPair(HexVar a, HexVar b, World world, HexContext hexContext, String glyphName) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        Vector3d posA = HexVarUtil.position(a, accessor);
        Vector3d posB = HexVarUtil.position(b, accessor);
        if (posA == null || posB == null)
            return;

        if (a instanceof EntityVar || b instanceof EntityVar) {
            moveToDestination(a, posB, world, hexContext, glyphName);
            moveToDestination(b, posA, world, hexContext, glyphName);
            return;
        }

        int ax = (int) Math.floor(posA.x());
        int ay = (int) Math.floor(posA.y());
        int az = (int) Math.floor(posA.z());
        int bx = (int) Math.floor(posB.x());
        int by = (int) Math.floor(posB.y());
        int bz = (int) Math.floor(posB.z());

        if (!HexProtection.canModifyBlock(world, caster, accessor, new Vector3i(ax, ay, az), BlockAction.BREAK)
                || !HexProtection.canModifyBlock(world, caster, accessor, new Vector3i(bx, by, bz),
                        BlockAction.BREAK)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }

        WorldChunk chunkA = world.getChunk(ChunkUtil.indexChunkFromBlock(ax, az));
        WorldChunk chunkB = world.getChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
        if (chunkA == null || chunkB == null)
            return;

        if (isMultiCell(world, ax, ay, az) || isMultiCell(world, bx, by, bz)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }

        BlockSelection selA = new BlockSelection();
        selA.setPosition(ax, ay, az);
        selA.copyFromAtWorld(ax, ay, az, chunkA, null);

        BlockSelection selB = new BlockSelection();
        selB.setPosition(bx, by, bz);
        selB.copyFromAtWorld(bx, by, bz, chunkB, null);

        selA.setPosition(bx, by, bz);
        selB.setPosition(ax, ay, az);
        selA.placeNoReturn(world, new Vector3i(), accessor);
        selB.placeNoReturn(world, new Vector3i(), accessor);
    }
}
