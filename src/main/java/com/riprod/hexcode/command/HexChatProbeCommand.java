package com.riprod.hexcode.command;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HexChatProbeCommand extends AbstractPlayerCommand {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final int DEFAULT_PACKETS = 30;
    private static final int DEFAULT_CHILDREN = 300;
    private static final int DEFAULT_REAL_PACKETS = 30;
    private static final int DEFAULT_REAL_LINES = 10;

    private final RequiredArg<String> modeArg;
    private final OptionalArg<Integer> countArg;
    private final OptionalArg<Integer> linesArg;

    public HexChatProbeCommand() {
        super("chatprobe", "Probe the client chat crash threshold (packets vs rendered lines)");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);

        this.modeArg = this.withRequiredArg("mode", "packets | lines | real", ArgTypes.STRING);
        this.countArg = this.withOptionalArg("count", "packets to send, or children for lines mode",
                ArgTypes.INTEGER);
        this.linesArg = this.withOptionalArg("lines", "children per packet in real mode (default 10)",
                ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> playerEntityRef, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String mode = modeArg.get(context);
        if (mode == null) {
            return;
        }

        switch (mode.toLowerCase()) {
            case "packets" -> {
                int count = resolve(countArg.get(context), DEFAULT_PACKETS);
                LOGGER.atInfo().log("[hexcode] chatprobe packets count=%s player=%s", count,
                        playerRef.getUsername());
                for (int i = 0; i < count; i++) {
                    playerRef.sendMessage(Message.raw("probe " + i));
                }
            }
            case "lines" -> {
                int count = resolve(countArg.get(context), DEFAULT_CHILDREN);
                LOGGER.atInfo().log("[hexcode] chatprobe lines children=%s player=%s", count,
                        playerRef.getUsername());
                playerRef.sendMessage(composite(count, "line "));
            }
            case "real" -> {
                int count = resolve(countArg.get(context), DEFAULT_REAL_PACKETS);
                int lines = resolve(linesArg.get(context), DEFAULT_REAL_LINES);
                LOGGER.atInfo().log("[hexcode] chatprobe real packets=%s lines=%s total=%s player=%s",
                        count, lines, count * lines, playerRef.getUsername());
                for (int i = 0; i < count; i++) {
                    playerRef.sendMessage(composite(lines, "dump " + i + " line "));
                }
            }
            case "markup" -> {
                int count = resolve(countArg.get(context), DEFAULT_REAL_PACKETS);
                int lines = resolve(linesArg.get(context), DEFAULT_REAL_LINES);
                LOGGER.atInfo().log("[hexcode] chatprobe markup packets=%s lines=%s player=%s",
                        count, lines, playerRef.getUsername());
                for (int i = 0; i < count; i++) {
                    playerRef.sendMessage(dumpLike(lines));
                }
            }
            case "markupone" -> {
                int count = resolve(countArg.get(context), DEFAULT_REAL_PACKETS);
                int lines = resolve(linesArg.get(context), DEFAULT_REAL_LINES);
                LOGGER.atInfo().log("[hexcode] chatprobe markupone dumps=%s lines=%s player=%s",
                        count, lines, playerRef.getUsername());
                Message composite = Message.raw("");
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        composite.insert("\n");
                    }
                    composite.insert(dumpLike(lines));
                }
                playerRef.sendMessage(composite);
            }
            default -> playerRef.sendMessage(
                    Message.raw("chatprobe: mode must be packets, lines, real, markup or markupone"));
        }
    }

    private static int resolve(Integer value, int fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private static Message dumpLike(int lines) {
        Message inner = Message.raw("");
        for (int i = 0; i < lines; i++) {
            if (i > 0) {
                inner.insert("\n");
            }
            inner.insert(markup(Message.translation("hexcode.debugGlyph.slots.line")
                    .param("index", i)
                    .param("value", "42.00")
                    .param("glyph", "Number")
                    .param("accuracy", "1.00")
                    .param("speed", "1.00")
                    .param("metadata", "Number")));
        }
        return markup(Message.translation("hexcode.debugGlyph.slots")
                .param("volatility", "1.0 / 1.0")
                .param("complexity", "Arcane=1.0")
                .param("slots", inner));
    }

    private static Message markup(Message message) {
        message.getFormattedMessage().markupEnabled = true;
        return message;
    }

    private static Message composite(int children, String prefix) {
        Message composite = Message.raw("");
        for (int i = 0; i < children; i++) {
            if (i > 0) {
                composite.insert("\n");
            }
            composite.insert(Message.raw(prefix + i));
        }
        return composite;
    }
}
