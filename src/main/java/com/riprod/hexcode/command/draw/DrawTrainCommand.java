package com.riprod.hexcode.command.draw;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.riprod.hexcode.core.common.drawing.DrawCaptureService;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;

public class DrawTrainCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> shapeIdArg;
    private final OptionalArg<String> packArg;

    public DrawTrainCommand() {
        super("train", "Record next drawn shape as a template for the given shape ID");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        addAliases("t");

        this.shapeIdArg = this.withRequiredArg("shapeId", "The shape ID to train", ArgTypes.STRING);
        this.packArg = this.withOptionalArg("pack",
                "target asset pack (group:name) to write the template into; defaults to shape's owner pack",
                ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        String shapeId = shapeIdArg.get(context);

        ShapeAsset asset = ShapeAsset.getAssetMap().getAsset(shapeId);
        if (asset == null) {
            playerRef.sendMessage(Message.raw("unknown shape: " + shapeId));
            return;
        }

        UUIDComponent uuid = store.getComponent(playerEntityRef, UUIDComponent.getComponentType());
        if (uuid == null) {
            playerRef.sendMessage(Message.raw("no uuid component found"));
            return;
        }

        String overridePack = packArg.provided(context) ? packArg.get(context) : null;
        DrawCaptureService.requestTraining(uuid.getUuid(), shapeId, overridePack);

        String suffix = overridePack != null ? " (writing to pack '" + overridePack + "')" : "";
        playerRef.sendMessage(Message.raw("training mode: draw a '" + shapeId + "' now. next shape will be recorded." + suffix));
    }
}
