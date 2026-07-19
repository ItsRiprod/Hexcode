package com.riprod.hexcode.command;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.player.AddOrUpdateTriggerVolumeDisplay;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeDisplayEntry;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeShapeType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class HexTestVisualCommand extends AbstractPlayerCommand {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    static final String VOLUME_ID = "hexcode_testvisual";
    static final float DEFAULT_RADIUS = 3.0f;

    static final Vector3f SHELL_COLOR = new Vector3f(0.0f, 0.8f, 0.8f);
    static final float SHELL_OPACITY = 0.0f;

    private final OptionalArg<Float> radiusArg;

    public HexTestVisualCommand() {
        super("testvisual", "Send a trigger-volume sphere at your position for visual testing");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);
        addAliases("tv");

        this.radiusArg = this.withOptionalArg("radius", "Sphere radius in blocks (default 3.0)", ArgTypes.FLOAT);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Float radius = radiusArg.get(context);
        if (radius == null || radius <= 0f) {
            radius = DEFAULT_RADIUS;
        }

        Vector3d eye = PlayerUtils.getPlayerEyePosition(store, playerEntityRef);
        Vector3f center = new Vector3f((float) eye.x, (float) eye.y, (float) eye.z);

        playerRef.getPacketHandler().write(buildPacket(center, radius));

        playerRef.sendMessage(Message.raw(String.format(
                "(debug) sphere volume r=%.1f at %.1f, %.1f, %.1f", radius, eye.x, eye.y, eye.z)));
        LOGGER.atInfo().log("testvisual sphere r=%s at %s", radius, eye);
    }

    static TriggerVolumeDisplayEntry buildSphereEntry(@Nonnull Vector3f center, float radius) {
        TriggerVolumeDisplayEntry entry = new TriggerVolumeDisplayEntry();
        entry.shapeType = TriggerVolumeShapeType.Sphere;
        entry.position = center;
        entry.dimensions = new Vector3f(radius, 0f, 0f);
        entry.color = SHELL_COLOR;
        entry.opacity = SHELL_OPACITY;
        // entry.name = VOLUME_ID;
        return entry;
    }

    static AddOrUpdateTriggerVolumeDisplay buildPacket(@Nonnull Vector3f center, float radius) {
        return new AddOrUpdateTriggerVolumeDisplay(VOLUME_ID, buildSphereEntry(center, radius));
    }
}
