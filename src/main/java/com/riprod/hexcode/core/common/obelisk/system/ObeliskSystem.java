package com.riprod.hexcode.core.common.obelisk.system;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;

public class ObeliskSystem {

    public static void updateState(CommandBuffer<EntityStore> buffer, PedestalBlockComponent pedestal,
            World world, PedestalState previousState, PedestalState newState) {

        List<Vector3i> obeliskPositions = pedestal.getActiveObelisks();

        String blockState = switch (newState) {
            case IDLE -> "Idle";
            case READY -> "Ready";
            case SELECTING -> "Selecting";
            case CRAFTING -> "Crafting";
        };

        for (Vector3i obeliskPos : obeliskPositions) {
            if (obeliskPos == null) continue;
            PedestalBlockUtil.changeBlockState(world, obeliskPos, blockState);
        }

        ObeliskDispatcher.dispatchStateChange(buffer, pedestal, previousState, newState);

        if (newState == PedestalState.IDLE) {
            for (Vector3i obeliskPos : obeliskPositions) {
                if (obeliskPos == null) continue;
                ObeliskBlockComponent obelisk = BlockModule.getComponent(
                        ObeliskBlockComponent.getComponentType(), world,
                        obeliskPos.x, obeliskPos.y, obeliskPos.z);
                if (obelisk != null) {
                    obelisk.clearRegistration();
                }
            }
            pedestal.getActiveObelisks().clear();
        }
    }

    public static void cleanupObelisks(CommandBuffer<EntityStore> buffer, World world, List<Vector3i> obelisks) {
        if (obelisks.isEmpty()) return;

        for (Vector3i obeliskPos : obelisks) {
            if (obeliskPos == null) continue;
            ObeliskBlockComponent comp = BlockModule.getComponent(
                    ObeliskBlockComponent.getComponentType(), world,
                    obeliskPos.x, obeliskPos.y, obeliskPos.z);
            if (comp != null) {
                comp.clearRegistration();
            }
            PedestalBlockUtil.changeBlockState(world, obeliskPos, "Idle");
        }
    }
}
