package com.riprod.hexcode.command.glyph;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;

import javax.annotation.Nonnull;

public class GlyphsListCommand extends AbstractPlayerCommand {

    public GlyphsListCommand() {
        super("list", "List all available glyphs");
        addAliases("li");
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        context.sendMessage(Message.raw("Total Glyphs: " + GlyphAsset.getAssetMap().getAssetMap().size()));
        for (GlyphAsset glyphAsset : GlyphAsset.getAssetMap().getAssetMap().values()) {
            GlyphHandler handler = GlyphRegistry.get(glyphAsset.getHandler());
            if (handler != null) {
                continue;
            }
            context.sendMessage(Message.raw("- " + glyphAsset.getId()));
        }

    }
}
