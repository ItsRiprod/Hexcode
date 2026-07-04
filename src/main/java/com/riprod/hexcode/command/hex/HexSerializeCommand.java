package com.riprod.hexcode.command.hex;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexes.codec.DecodeIssue;
import com.riprod.hexcode.core.common.hexes.codec.DecodeResult;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.utils.HexSlot;

public class HexSerializeCommand extends AbstractPlayerCommand {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexSerializeCommand() {
        super("serialize", "import/export hex spells");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        addAliases("s");
        addSubCommand(new ExportCommand());
        addSubCommand(new ImportCommand());
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        send(playerRef, "usage: /hexcode serialize export");
        send(playerRef, "usage: /hexcode serialize import <data>");
    }

    private static class ExportCommand extends AbstractPlayerCommand {

        ExportCommand() {
            super("export", "export the active hex on your staff");
            addAliases("e");
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

            var execComp = store.getComponent(playerEntityRef, ExecutionComponent.getComponentType());

            if (execComp == null) {
                send(playerRef, "no execution component found on player");
                return;
            }

            Hex hex = execComp.getQueuedHex();
            if (hex == null) {
                send(playerRef, "no hex selected on your staff");
                return;
            }

            String data = HexUtils.serialize(hex);
            send(playerRef, "exported hex:");
            send(playerRef, data);
        }
    }

    private static class ImportCommand extends AbstractPlayerCommand {

        private final RequiredArg<String> dataArg;

        ImportCommand() {
            super("import", "import a hex into your book");
            addAliases("i");
            dataArg = withRequiredArg("data", "hex data string", ArgTypes.STRING);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {

            String data = context.get(dataArg);

            DecodeResult result = HexUtils.deserializeWithResult(data);
            if (result.getHex() == null) {
                send(playerRef, "invalid hex data - check that you copied the full string");
                for (DecodeIssue issue : result.getIssues()) {
                    send(playerRef, "  " + issue);
                }
                return;
            }

            Hex hex = result.getHex();
            for (DecodeIssue issue : result.getIssues()) {
                if (issue.getSeverity() != DecodeIssue.Severity.INFO) {
                    send(playerRef, "  " + issue);
                }
            }

            boolean ok = CasterInventory.addHexToBook(store, playerEntityRef, HexSlot.OffHand, hex);
            if (!ok) {
                send(playerRef, "you need to hold a hex book in your off hand (or it is full)");
                return;
            }
            send(playerRef, "imported hex into book");
        }
    }

    private static void send(PlayerRef playerRef, String message) {
        playerRef.sendMessage(Message.raw(message));
        LOGGER.atInfo().log(message);
    }
}
