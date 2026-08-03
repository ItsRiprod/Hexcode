package com.riprod.hexcode.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.command.draw.DrawTrainCommand;
import com.riprod.hexcode.command.glyph.GlyphsForgetCommand;
import com.riprod.hexcode.command.glyph.GlyphsLearnCommand;
import com.riprod.hexcode.command.glyph.GlyphsListCommand;
import com.riprod.hexcode.command.hex.HexCastCommand;
import com.riprod.hexcode.command.hex.HexInspectCommand;
import com.riprod.hexcode.command.hex.HexSaveCommand;
import com.riprod.hexcode.command.hex.HexSerializeCommand;
import com.riprod.hexcode.command.hex.HexTestRoundtripCommand;
import com.riprod.hexcode.builtin.hextreme.command.HexPageCommand;

import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;

public class HexcodeCommand extends AbstractPlayerCommand {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public HexcodeCommand() {
        super("hexcode", "Hexcode spell-crafting mod commands");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);
        addAliases("hc");

        addSubCommand(new GlyphsLearnCommand());
        addSubCommand(new GlyphsListCommand());
        addSubCommand(new GlyphsForgetCommand());
        addSubCommand(new HexInspectCommand());
        addSubCommand(new HexSerializeCommand());
        addSubCommand(new HexSaveCommand());
        addSubCommand(new HexTestRoundtripCommand());
        addSubCommand(new HexCastCommand());
        addSubCommand(new DrawTrainCommand());
        addSubCommand(new HexResetCommand());
        addSubCommand(new HexPageCommand());
        addSubCommand(new HexTestVisualCommand());
        addSubCommand(new HexChatProbeCommand());
        // addSubCommand(new HexStopCommand());
        // addSubCommand(new HexResumeCommand());
        // addSubCommand(new HexTimeoutCommand());
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref,
            PlayerRef playerRef, World world) {
        try {
            showHelp(ctx);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexcodeCommand failed: %s", e.getMessage());
        }
    }

    private void showHelp(CommandContext ctx) {
        ctx.sendMessage(Message.raw("Hexcode Commands:"));
        ctx.sendMessage(Message.raw("/hexcode learn - Learn the glyph you are looking at"));
        ctx.sendMessage(Message.raw("/hexcode list - List the glyphs you have"));
        ctx.sendMessage(Message.raw("/hexcode forget - Forget the glyph you are looking at"));
        ctx.sendMessage(Message.raw("/hexcode inspect - Print the glyph tree of the active hex on the held staff"));
        ctx.sendMessage(Message.raw("/hexcode serialize - Print the serialized data of the active hex on the held staff"));
        ctx.sendMessage(Message.raw("/hexcode save <name> --pack=<packName> - Save the active hex as a reusable preset"));
        ctx.sendMessage(Message.raw("/hexcode test-roundtrip - Encode+decode the active hex and verify structural equality"));
        ctx.sendMessage(Message.raw("/hexcode cast <hexId> - Cast a saved hex by its asset id"));
        ctx.sendMessage(Message.raw("/hexcode page <hexId> --name=<override> --quantity=<n> - Create Spell Page item(s) inscribed with a saved hex"));
        ctx.sendMessage(Message.raw("/hexcode train - Start a draw training session"));
        ctx.sendMessage(Message.raw("/hexcode reset - Force reset hexcode state to IDLE"));
        ctx.sendMessage(Message.raw("/hexcode testvisual --radius=<r> - Send a trigger-volume sphere at your position"));
    }
}
