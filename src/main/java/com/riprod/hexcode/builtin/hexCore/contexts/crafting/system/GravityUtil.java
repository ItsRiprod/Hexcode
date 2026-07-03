package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.protocol.packets.player.UpdateMovementSettings;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class GravityUtil {

    private static final float FLY_HORIZONTAL_SPEED = 3.0f;
    private static final float FLY_VERTICAL_SPEED = 2.0f;

    public static void enterFly(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef) {
        MovementManager mm = buffer.getComponent(playerRef, MovementManager.getComponentType());
        if (mm == null) return;

        MovementSettings settings = mm.getSettings();
        settings.canFly = true;
        settings.horizontalFlySpeed = FLY_HORIZONTAL_SPEED;
        settings.verticalFlySpeed = FLY_VERTICAL_SPEED;

        PacketHandler handler = getPacketHandler(buffer, playerRef);
        if (handler == null) return;

        handler.writeNoCache(new UpdateMovementSettings(settings));
        handler.writeNoCache(new SetMovementStates(new SavedMovementStates(true)));
    }

    public static void exitFly(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef) {
        MovementManager mm = buffer.getComponent(playerRef, MovementManager.getComponentType());
        if (mm == null) return;

        PacketHandler handler = getPacketHandler(buffer, playerRef);
        if (handler == null) return;

        handler.writeNoCache(new SetMovementStates(new SavedMovementStates(false)));

        mm.applyDefaultSettings();
        handler.writeNoCache(new UpdateMovementSettings(mm.getSettings()));
    }

    private static PacketHandler getPacketHandler(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> playerRef) {
        PlayerRef pr = buffer.getComponent(playerRef, PlayerRef.getComponentType());
        return pr != null ? pr.getPacketHandler() : null;
    }
}
