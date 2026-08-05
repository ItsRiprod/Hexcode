package com.riprod.hexcode.core.common.pedestal.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;

public class PedestalBlockUtil {

    public static void changeBlockState(World world, Vector3i pos, String stateName) {
        WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (chunk == null) {
            return;
        }

        BlockType blockType = chunk.getBlockType(pos.x, pos.y, pos.z);
        if (blockType == null || blockType.isUnknown()) {
            return;
        }

        String baseKey = blockType.getDefaultStateKey();
        if (baseKey != null) {
            BlockType baseType = BlockType.getAssetMap().getAsset(baseKey);
            if (baseType != null) {
                blockType = baseType;
            }
        }

        chunk.setBlockInteractionState(pos, blockType, stateName);
    }

    public static PedestalBlockComponent resolvePedestal(Ref<EntityStore> playerRef,
            ComponentAccessor<EntityStore> buffer) {

        HexcodeSessionComponent session = buffer.getComponent(playerRef,
                HexcodeSessionComponent.getComponentType());
        if (session == null) {
            HexcasterCraftingComponent craftingComp = buffer.getComponent(playerRef,
                    HexcasterCraftingComponent.getComponentType());
            if (craftingComp == null || !craftingComp.hasActiveSession()) return null;
            session = buffer.getComponent(craftingComp.getSessionRef(),
                    HexcodeSessionComponent.getComponentType());
            if (session == null) return null;
        }

        Vector3i pos = session.getPedestalLocation();
        return BlockModule.getComponent(
                PedestalBlockComponent.getComponentType(),
                buffer.getExternalData().getWorld(),
                pos.x(), pos.y(), pos.z());
    }
}
