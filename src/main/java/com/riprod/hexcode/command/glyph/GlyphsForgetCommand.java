package com.riprod.hexcode.command.glyph;

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
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.utils.HexSlot;

import javax.annotation.Nonnull;

public class GlyphsForgetCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> glyphIdArg;

    public GlyphsForgetCommand() {
        super("forget", "Forget a glyph from held hexbook");
        addAliases("f");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);

        this.glyphIdArg = this.withRequiredArg("glyphId", "The glyph ID to forget", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        String glyphId = glyphIdArg.get(context);

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (asset == null) {
            playerRef.sendMessage(Message.raw("Unknown glyph: " + glyphId));
            return;
        }

        boolean removed = CasterInventory.removeGlyphFromBook(store, playerEntityRef, HexSlot.Both, asset.getId());

        if (removed) {
            playerRef.sendMessage(Message.raw("(debug) Forgot glyph '" + glyphId + "' from your hexbook!"));
        } else {
            playerRef.sendMessage(Message.raw("(debug) Glyph '" + glyphId + "' was not found in your hexbook."));
        }
    }
}
