package com.riprod.hexcode.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;

public class BlockUtils {
    public static void moveBlock(Vector3i source, Vector3d destination, World world, CommandBuffer<EntityStore> accessor) {
        int srcX = source.x();
        int srcY = source.y();
        int srcZ = source.z();

        WorldChunk srcChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(srcX, srcZ));
        if (srcChunk == null || srcChunk.getBlock(srcX, srcY, srcZ) == BlockType.EMPTY_ID)
            return;

        int destX = (int) Math.floor(destination.x());
        int destY = (int) Math.floor(destination.y());
        int destZ = (int) Math.floor(destination.z());

        Vector3i placement = findAirBlock(world, destX, destY, destZ);
        if (placement == null) {
            return;
        }

        // capture before clear so the block-entity state survives the move
        BlockSelection moved = new BlockSelection();
        moved.setPosition(srcX, srcY, srcZ);
        moved.copyFromAtWorld(srcX, srcY, srcZ, srcChunk, null);

        BlockSelection cleared = new BlockSelection();
        cleared.setPosition(srcX, srcY, srcZ);
        cleared.addEmptyAtWorldPos(srcX, srcY, srcZ);
        cleared.placeNoReturn(world, new Vector3i(), accessor);

        moved.setPosition(placement.x(), placement.y(), placement.z());
        moved.placeNoReturn(world, new Vector3i(), accessor);
    }

    public static Vector3i findAirBlock(World world, int x, int y, int z) {
        return findAirBlock(world, x, y, z, 5);
    }

    public static Vector3i findAirBlock(World world, int x, int y, int z, int maxSearchRadius) {
        if (world.getBlock(x, y, z) == 0)
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
                        if (world.getBlock(cx, cy, cz) == 0) {
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

    public static void moveToDestination(HexVar var, Vector3d dest, World world, HexContext hexContext) {
        if (var instanceof EntityVar entityVar) {
            Ref<EntityStore> entityRef = entityVar.getRef(hexContext.getAccessor());
            if (entityRef == null || !entityRef.isValid()) return;

            TransformComponent tc = hexContext.getAccessor().getComponent(entityRef,
                    TransformComponent.getComponentType());
            if (tc == null) return;

            Player player = hexContext.getAccessor().getComponent(entityRef, Player.getComponentType());
            if (player != null) {
                Rotation3f rotation = tc.getRotation();
                Teleport teleport = Teleport.createForPlayer(dest, rotation);
                hexContext.getAccessor().addComponent(entityRef, Teleport.getComponentType(), teleport);
            } else {
                tc.setPosition(new Vector3d(dest.x(), dest.y(), dest.z()));
            }
        } else if (var instanceof BlockVar blockVar && blockVar.getValue() != null) {
            moveBlock(blockVar.getValue(), dest, world, hexContext.getAccessor());
        } else if (var instanceof PositionVar posVar && posVar.getValue() != null) {
            Vector3i sourceBlock = new Vector3i(
                    (int) Math.floor(posVar.getValue().x()),
                    (int) Math.floor(posVar.getValue().y()),
                    (int) Math.floor(posVar.getValue().z()));
            moveBlock(sourceBlock, dest, world, hexContext.getAccessor());
        }
    }

    public static void swapPair(HexVar a, HexVar b, World world, HexContext hexContext) {
        Vector3d posA = HexVarUtil.position(a, hexContext.getAccessor());
        Vector3d posB = HexVarUtil.position(b, hexContext.getAccessor());
        if (posA == null || posB == null) return;

        // an entity on either side can't be block-captured; fall back to the teleport-based move
        if (a instanceof EntityVar || b instanceof EntityVar) {
            moveToDestination(a, posB, world, hexContext);
            moveToDestination(b, posA, world, hexContext);
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        int ax = (int) Math.floor(posA.x());
        int ay = (int) Math.floor(posA.y());
        int az = (int) Math.floor(posA.z());
        int bx = (int) Math.floor(posB.x());
        int by = (int) Math.floor(posB.y());
        int bz = (int) Math.floor(posB.z());

        WorldChunk chunkA = world.getChunk(ChunkUtil.indexChunkFromBlock(ax, az));
        WorldChunk chunkB = world.getChunk(ChunkUtil.indexChunkFromBlock(bx, bz));
        if (chunkA == null || chunkB == null) return;

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
