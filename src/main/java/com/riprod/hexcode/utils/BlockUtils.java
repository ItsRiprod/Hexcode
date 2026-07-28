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
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.PlaceBlockSettings;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.protection.BlockAction;
import com.riprod.hexcode.core.common.protection.HexProtection;

public class BlockUtils {

    private static final Rotation3f PRESERVE_ROTATION =
            new Rotation3f(Float.NaN, Float.NaN, Float.NaN);
    private static final int DEFAULT_SEARCH_RADIUS = 5;

    public static Box resolveBlockDisplayBox(World world, Vector3i blockPos) {
        int rotation = world.getBlockRotationIndex(blockPos.x(), blockPos.y(), blockPos.z());
        BlockType blockType = BlockType.getAssetMap().getAsset(world.getBlock(blockPos.x(), blockPos.y(), blockPos.z()));
        if (blockType == null) return null;
        var hitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
        if (hitbox == null) return null;
        return hitbox.get(rotation).getBoundingBox().clone();
    }

    public static void moveBlock(Vector3i source, Vector3d destination, World world) {
        BlockMove move = planMove(world, source, destination);
        if (move != null) applyMove(world, move);
    }

    private record BlockMove(int baseX, int baseY, int baseZ, int destX, int destY, int destZ,
            BlockType blockType, int rotationIndex) {
    }

    private static BlockMove planMove(World world, Vector3i source, Vector3d destination) {
        if (source.y() < ChunkUtil.MIN_Y || source.y() >= ChunkUtil.HEIGHT) return null;

        int blockId = world.getBlock(source.x(), source.y(), source.z());
        if (blockId == BlockType.EMPTY_ID) return null;
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null) return null;

        BlockSection sourceSection = blockSection(world, source.x(), source.y(), source.z());
        if (sourceSection == null) return null;

        int filler = sourceSection.getFiller(source.x(), source.y(), source.z());
        int baseX = source.x() - FillerBlockUtil.unpackX(filler);
        int baseY = source.y() - FillerBlockUtil.unpackY(filler);
        int baseZ = source.z() - FillerBlockUtil.unpackZ(filler);
        if (baseY < ChunkUtil.MIN_Y || baseY >= ChunkUtil.HEIGHT) return null;

        BlockSection baseSection = blockSection(world, baseX, baseY, baseZ);
        if (baseSection == null || baseSection.get(baseX, baseY, baseZ) != blockId) return null;
        int rotationIndex = baseSection.getRotationIndex(baseX, baseY, baseZ);

        // the anchor has to admit the whole rotated footprint, and the cells the block is vacating
        // count as free because the source is broken before the destination is written
        Vector3i placement = findAirBlock(world,
                (int) Math.floor(destination.x()),
                (int) Math.floor(destination.y()),
                (int) Math.floor(destination.z()),
                DEFAULT_SEARCH_RADIUS,
                (cx, cy, cz) -> world.testPlaceBlock(cx, cy, cz, blockType, rotationIndex,
                        (fx, fy, fz, cellType, cellRotation, cellFiller) ->
                                fx - FillerBlockUtil.unpackX(cellFiller) == baseX
                                        && fy - FillerBlockUtil.unpackY(cellFiller) == baseY
                                        && fz - FillerBlockUtil.unpackZ(cellFiller) == baseZ));
        if (placement == null) return null;

        return new BlockMove(baseX, baseY, baseZ, placement.x(), placement.y(), placement.z(),
                blockType, rotationIndex);
    }

    private static void applyMove(World world, BlockMove move) {
        WorldChunk sourceChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(move.baseX(), move.baseZ()));
        WorldChunk destChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(move.destX(), move.destZ()));
        if (sourceChunk == null || destChunk == null) return;

        Holder<ChunkStore> holder = world.getBlockComponentHolder(move.baseX(), move.baseY(), move.baseZ());
        ItemContainerBlock live = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(), world, move.baseX(), move.baseY(), move.baseZ());
        if (live != null) live.getItemContainer().dropAllItemStacks();

        if (!world.breakBlock(move.baseX(), move.baseY(), move.baseZ(), SetBlockSettings.NO_SEND_PARTICLES)) {
            reattachBlockEntity(world, sourceChunk, move.baseX(), move.baseY(), move.baseZ(),
                    move.blockType(), move.rotationIndex(), holder);
            return;
        }

        destChunk.placeBlock(move.destX(), move.destY(), move.destZ(), move.blockType().getId(),
                RotationTuple.get(move.rotationIndex()), PlaceBlockSettings.PERFORM_BLOCK_UPDATE, false);
        reattachBlockEntity(world, destChunk, move.destX(), move.destY(), move.destZ(),
                move.blockType(), move.rotationIndex(), holder);
        markPhysicsExempt(world, move.blockType(), move.rotationIndex(),
                move.destX(), move.destY(), move.destZ());
    }

    public static boolean rotateBlock(Vector3i target, Rotation3f rotation, World world,
            CommandBuffer<EntityStore> accessor, Ref<EntityStore> caster, String glyphName) {
        if (target.y() < ChunkUtil.MIN_Y || target.y() >= ChunkUtil.HEIGHT) return false;

        int blockId = world.getBlock(target.x(), target.y(), target.z());
        if (blockId == BlockType.EMPTY_ID) return false;
        BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
        if (blockType == null) return false;

        BlockSection hitSection = blockSection(world, target.x(), target.y(), target.z());
        if (hitSection == null) return false;

        int filler = hitSection.getFiller(target.x(), target.y(), target.z());
        int baseX = target.x() - FillerBlockUtil.unpackX(filler);
        int baseY = target.y() - FillerBlockUtil.unpackY(filler);
        int baseZ = target.z() - FillerBlockUtil.unpackZ(filler);
        if (baseY < ChunkUtil.MIN_Y || baseY >= ChunkUtil.HEIGHT) return false;

        BlockSection baseSection = blockSection(world, baseX, baseY, baseZ);
        if (baseSection == null || baseSection.get(baseX, baseY, baseZ) != blockId) return false;

        WorldChunk baseChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(baseX, baseZ));
        if (baseChunk == null) return false;

        int oldRotation = baseSection.getRotationIndex(baseX, baseY, baseZ);
        RotationTuple newRotation = RotationTuple.of(
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.yaw())),
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.pitch())),
                Rotation.closestOfDegrees((float) Math.toDegrees(rotation.roll())));
        if (newRotation.index() == oldRotation) return true;

        // the rotated footprint must be clear, except for the cells this block already occupies
        boolean fits = world.testPlaceBlock(baseX, baseY, baseZ, blockType, newRotation.index(),
                (cx, cy, cz, cellType, cellRotation, cellFiller) ->
                        cx - FillerBlockUtil.unpackX(cellFiller) == baseX
                                && cy - FillerBlockUtil.unpackY(cellFiller) == baseY
                                && cz - FillerBlockUtil.unpackZ(cellFiller) == baseZ);
        if (!fits) return false;

        Vector3i base = new Vector3i(baseX, baseY, baseZ);
        if (!HexProtection.canModifyBlock(world, caster, accessor, new Vector3i(base), BlockAction.BREAK)
                || !HexProtection.canModifyBlock(world, caster, accessor, new Vector3i(base), BlockAction.PLACE)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return false;
        }

        Holder<ChunkStore> holder = world.getBlockComponentHolder(baseX, baseY, baseZ);
        ItemContainerBlock live = BlockModule.getComponent(
                ItemContainerBlock.getComponentType(), world, baseX, baseY, baseZ);
        if (live != null) live.getItemContainer().dropAllItemStacks();

        // the captured copy is the only remaining owner of the contents once the live container is
        // emptied, so it has to be reinstated on every exit from here on
        if (!world.breakBlock(baseX, baseY, baseZ, SetBlockSettings.NO_SEND_PARTICLES)) {
            reattachBlockEntity(world, baseChunk, baseX, baseY, baseZ, blockType, oldRotation, holder);
            return false;
        }
        baseChunk.placeBlock(baseX, baseY, baseZ, blockType.getId(), newRotation,
                PlaceBlockSettings.PERFORM_BLOCK_UPDATE, false);
        reattachBlockEntity(world, baseChunk, baseX, baseY, baseZ, blockType, newRotation.index(), holder);
        markPhysicsExempt(world, blockType, newRotation.index(), baseX, baseY, baseZ);
        return true;
    }

    private static void reattachBlockEntity(World world, WorldChunk chunk, int x, int y, int z,
            BlockType blockType, int rotation, Holder<ChunkStore> holder) {
        if (holder == null) return;
        Ref<ChunkStore> chunkRef = chunk.getReference();
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        BlockComponentChunk componentChunk = chunkStore.getComponent(
                chunkRef, BlockComponentChunk.getComponentType());
        if (componentChunk == null) return;
        BlockEntity.setBlockEntity(chunkStore, chunkRef, componentChunk, x, y, z, blockType, rotation, holder);
    }

    /**
     * Exempts every cell of the placed footprint from block physics. Required support faces rotate
     * with the block ({@code BlockType.getSupport(rotation)}), so a block moved or turned out of its
     * authored orientation is usually unsupported and would be broken by the physics tick.
     */
    private static void markPhysicsExempt(World world, BlockType blockType, int rotationIndex,
            int baseX, int baseY, int baseZ) {
        BlockBoundingBoxes hitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
        if (hitbox == null) {
            markDecoAt(world, baseX, baseY, baseZ);
            return;
        }
        FillerBlockUtil.forEachFillerBlock(hitbox.get(rotationIndex),
                (dx, dy, dz) -> markDecoAt(world, baseX + dx, baseY + dy, baseZ + dz));
    }

    private static void markDecoAt(World world, int x, int y, int z) {
        if (y < ChunkUtil.MIN_Y || y >= ChunkUtil.HEIGHT) return;
        Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) return;
        BlockPhysics.markDeco(world.getChunkStore().getStore(), sectionRef, x, y, z);
    }

    private static boolean isMultiCell(World world, int x, int y, int z) {
        if (y < ChunkUtil.MIN_Y || y >= ChunkUtil.HEIGHT) return false;
        BlockType blockType = BlockType.getAssetMap().getAsset(world.getBlock(x, y, z));
        if (blockType == null) return false;
        BlockBoundingBoxes hitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
        return hitbox != null && hitbox.protrudesUnitBox();
    }

    private static BlockSection blockSection(World world, int x, int y, int z) {
        Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) return null;
        return world.getChunkStore().getStore().getComponent(sectionRef, BlockSection.getComponentType());
    }

    public static Vector3i findAirBlock(World world, int x, int y, int z) {
        return findAirBlock(world, x, y, z, DEFAULT_SEARCH_RADIUS);
    }

    public static Vector3i findAirBlock(World world, int x, int y, int z, int maxSearchRadius) {
        return findAirBlock(world, x, y, z, maxSearchRadius, null);
    }

    public static Vector3i findAirBlock(World world, int x, int y, int z, int maxSearchRadius,
            TriIntPredicate accepts) {
        if (world.getBlock(x, y, z) == 0 && (accepts == null || accepts.test(x, y, z)))
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
                        if (world.getBlock(cx, cy, cz) == 0
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
            if (entityRef == null || !entityRef.isValid()) return;

            if (!HexProtection.canAffectEntity(world, caster, accessor, entityRef)) {
                HexProtection.notifyBlocked(caster, accessor, glyphName);
                return;
            }

            if (accessor.getComponent(entityRef, TransformComponent.getComponentType()) == null) return;

            accessor.putComponent(entityRef, Teleport.getComponentType(),
                    new Teleport(dest, PRESERVE_ROTATION));
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
        if (move == null) return;

        // gated on the resolved base, since that is the cell whose removal takes the structure
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
        if (posA == null || posB == null) return;

        // an entity on either side can't be block-captured; fall back to the teleport-based move
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
                || !HexProtection.canModifyBlock(world, caster, accessor, new Vector3i(bx, by, bz), BlockAction.BREAK)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }

        WorldChunk chunkA = world.getChunk(ChunkUtil.indexChunkFromBlock(ax, az));
        WorldChunk chunkB = world.getChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
        if (chunkA == null || chunkB == null) return;

        // a single-cell capture of a multi-cell block orphans the rest of its structure, and two
        // footprints of different shapes have no well-defined in-place swap
        if (isMultiCell(world, ax, ay, az) || isMultiCell(world, bx, by, bz)) {
            HexProtection.notifyBlocked(caster, accessor, glyphName);
            return;
        }

        // capture both before writing either, otherwise the first place would corrupt the second capture
        BlockSelection selA = new BlockSelection();
        selA.setPosition(ax, ay, az);
        selA.copyFromAtWorld(ax, ay, az, chunkA, null);

        BlockSelection selB = new BlockSelection();
        selB.setPosition(bx, by, bz);
        selB.copyFromAtWorld(bx, by, bz, chunkB, null);

        // each place overwrites the other cell; the swap self-clears both sources
        selA.setPosition(bx, by, bz);
        selB.setPosition(ax, ay, az);
        selA.placeNoReturn(world, new Vector3i(), accessor);
        selB.placeNoReturn(world, new Vector3i(), accessor);
    }
}
