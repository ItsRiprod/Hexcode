package com.riprod.hexcode.command.glyph;

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
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphRegistry;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.utils.HexSlot;

import javax.annotation.Nonnull;

public class GlyphsLearnCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> glyphIdArg;
    private final OptionalArg<Float> accuracyArg;
    private final OptionalArg<Float> speedArg;

    public GlyphsLearnCommand() {
        super("learn", "Learn a glyph into held hexbook");
        addAliases("l");

        this.glyphIdArg = this.withRequiredArg("glyphId", "The glyph ID to learn", ArgTypes.STRING);
        this.accuracyArg = this.withOptionalArg("accuracy", "Accuracy modifier (default 1.0)", ArgTypes.FLOAT);
        this.speedArg = this.withOptionalArg("speed", "Speed modifier (default 1.0)", ArgTypes.FLOAT);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        String glyphId = glyphIdArg.get(context);
        Float accuracy = accuracyArg.get(context);
        Float speed = speedArg.get(context);

        if (accuracy == null) {
            accuracy = 1.0f;
        }
        if (speed == null) {
            speed = 1.0f;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (asset == null) {
            playerRef.sendMessage(Message.raw("Unknown glyph: " + glyphId));
            return;
        }

        GlyphHandler handler = GlyphRegistry.get(asset.getHandler());
        if (handler == null) {
            playerRef.sendMessage(Message.raw("No handler registered for glyph: " + glyphId));
            return;
        }

        Glyph glyph = new Glyph(asset, accuracy, speed);
        Hex hex = new Hex(glyph);

        boolean ok = CasterInventory.addHexToBook(store, playerEntityRef, HexSlot.Both, hex);

        if (!ok) {
            playerRef.sendMessage(Message.raw("No hexbook found in hand, or it is full"));
            return;
        }

        playerRef.sendMessage(Message
                .raw("(debug) Learned glyph '" + glyphId + "' (accuracy=" + accuracy + ", speed=" + speed + ")"));
    }
}
