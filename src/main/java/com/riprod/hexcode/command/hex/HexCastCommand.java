package com.riprod.hexcode.command.hex;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.saved.SavedHexAsset;

public class HexCastCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<String> hexIdArg =
            this.withRequiredArg("hexId", "saved hex asset id", ArgTypes.STRING);

    public HexCastCommand() {
        super("cast", "cast a saved hex by its asset id");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        String hexId = hexIdArg.get(context);

        SavedHexAsset asset = SavedHexAsset.getAssetMap().getAsset(hexId);
        if (asset == null) {
            playerRef.sendMessage(Message.raw("no saved hex found with id: " + hexId));
            return;
        }

        Hex hex = asset.getHex().clone();
        String name = asset.getDisplayName() != null ? asset.getDisplayName() : hexId;

        var hexStats = new HexCast();

        var playerHexRoot = new PlayerHexRoot(playerEntityRef, store);

        var castCtx = new HexContext(hex, 0, playerHexRoot, null, hexStats);

        store.invoke(new HexCastEvent(castCtx));

        playerRef.sendMessage(Message.raw("cast hex: " + name));
    }
}
