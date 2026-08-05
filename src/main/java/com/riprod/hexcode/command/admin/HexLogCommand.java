package com.riprod.hexcode.command.admin;

import java.util.Locale;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.riprod.hexcode.utils.LogScopes;

public class HexLogCommand extends CommandBase {

    private static final String PREFIX = "Hexcode|";
    private static final String ALL = "all";
    private static final String LIST = "list";

    private static final Level[] LEVELS = {
            Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO,
            Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL
    };

    private static final String[] SCOPE_NAMES = buildScopeNames();

    private static final SingleArgumentType<String> SCOPE = new SingleArgumentType<>(
            "scope", "a hexcode log scope, or 'all' / 'list'", SCOPE_NAMES) {
        @Override
        public String parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
            String resolved = resolveScope(input);
            if (resolved == null) {
                parseResult.fail(Message.raw("unknown log scope '" + input + "'. try /hc log list"));
                return null;
            }
            return resolved;
        }

        @Override
        public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered,
                int numParametersTyped, @Nonnull SuggestionResult result) {
            for (String name : SCOPE_NAMES) {
                result.suggest(name);
            }
        }
    };

    private static final SingleArgumentType<Level> LOG_LEVEL = new SingleArgumentType<>(
            "level", "a java.util.logging level", levelNames()) {
        @Override
        public Level parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
            try {
                return Level.parse(input.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                parseResult.fail(Message.raw("unknown level '" + input + "'"));
                return null;
            }
        }

        @Override
        public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered,
                int numParametersTyped, @Nonnull SuggestionResult result) {
            for (Level level : LEVELS) {
                result.suggest(level.getName());
            }
        }
    };

    @Nonnull
    private final RequiredArg<String> scopeArg =
            this.withRequiredArg("scope", "hexcode log scope, or 'all' / 'list'", SCOPE);

    @Nonnull
    private final OptionalArg<Level> levelArg =
            this.withOptionalArg("level", "log level to set; omit to read the current one", LOG_LEVEL);

    public HexLogCommand() {
        super("log", "read or set hexcode log scope levels");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        String scope = scopeArg.get(context);
        if (scope == null) return;

        if (LIST.equals(scope)) {
            context.sendMessage(Message.raw("hexcode log scopes:"));
            for (String full : LogScopes.ALL) {
                context.sendMessage(Message.raw("  " + shortName(full) + " (" + full + ") = "
                        + HytaleLogger.get(full).getLevel().getName()));
            }
            return;
        }

        if (!levelArg.provided(context)) {
            if (ALL.equals(scope)) {
                context.sendMessage(Message.raw("specify --level=<level> to set every scope at once"));
                return;
            }
            context.sendMessage(Message.raw(scope + " = " + HytaleLogger.get(scope).getLevel().getName()));
            return;
        }

        Level level = levelArg.get(context);
        if (level == null) return;

        if (ALL.equals(scope)) {
            for (String full : LogScopes.ALL) {
                HytaleLogger.get(full).setLevel(level);
            }
            context.sendMessage(Message.raw("set all " + LogScopes.ALL.length
                    + " hexcode scopes to " + level.getName()));
            return;
        }

        HytaleLogger.get(scope).setLevel(level);
        context.sendMessage(Message.raw("set " + scope + " to " + level.getName()));
    }

    private static String shortName(@Nonnull String full) {
        return full.startsWith(PREFIX) ? full.substring(PREFIX.length()) : full;
    }

    private static String resolveScope(@Nonnull String input) {
        if (input.equalsIgnoreCase(ALL)) return ALL;
        if (input.equalsIgnoreCase(LIST)) return LIST;
        for (String full : LogScopes.ALL) {
            if (input.equalsIgnoreCase(full) || input.equalsIgnoreCase(shortName(full))) {
                return full;
            }
        }
        return null;
    }

    private static String[] buildScopeNames() {
        String[] names = new String[LogScopes.ALL.length + 2];
        for (int i = 0; i < LogScopes.ALL.length; i++) {
            names[i] = shortName(LogScopes.ALL[i]);
        }
        names[LogScopes.ALL.length] = ALL;
        names[LogScopes.ALL.length + 1] = LIST;
        return names;
    }

    private static String[] levelNames() {
        String[] names = new String[LEVELS.length];
        for (int i = 0; i < LEVELS.length; i++) {
            names[i] = LEVELS[i].getName();
        }
        return names;
    }
}
