package com.riprod.hexcode.core.common.pedestal.utils;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.utils.BlockAccess;

public class PedestalBlockUtil {

    public static void changeBlockState(World world, Vector3i pos, String stateName) {
        BlockType blockType = BlockAccess.blockType(world, pos);
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

        BlockAccess.setInteractionState(world, pos, blockType, stateName);
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

    public static boolean outsideRadius(ComponentAccessor<EntityStore> buffer, Ref<EntityStore> player,
            PedestalBlockComponent pedestal) {
        Vector3i pedestalLoc = pedestal.getLocation();
        TransformComponent transform = buffer.getComponent(player, TransformComponent.getComponentType());
        if (pedestalLoc == null || transform == null || transform.getPosition() == null) {
            return false;
        }
        var center = new Vector3d(
                pedestalLoc.x() + 0.5,
                pedestalLoc.y() + 0.5,
                pedestalLoc.z() + 0.5);
        double maxRadius = pedestal.getMaxRadius();
        return transform.getPosition().distanceSquared(center) > maxRadius * maxRadius;
    }
}
