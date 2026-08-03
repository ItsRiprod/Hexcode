package com.riprod.hexcode.command.admin;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.gate.GateStateResource;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;

public class HexTimeoutCommand extends AbstractAsyncCommand {

    @Nonnull
    private final OptionalArg<Integer> durationArg =
            this.withOptionalArg("Duration", "server.hexcode.commands.timeout.duration.desc", ArgTypes.INTEGER);

    @Nonnull
    private final OptionalArg<PlayerRef> playerArg =
            this.withOptionalArg("player", "server.hexcode.commands.timeout.player.desc", ArgTypes.PLAYER_REF);

    @Nonnull
    private final OptionalArg<World> worldArg =
            this.withOptionalArg("world", "server.hexcode.commands.timeout.world.desc", ArgTypes.WORLD);

    public HexTimeoutCommand() {
        super("timeout", "server.hexcode.commands.timeout.desc");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!context.provided(durationArg)) {
            context.sendMessage(Message.raw("specify --duration=<seconds>"));
            return CompletableFuture.completedFuture(null);
        }
        Integer duration = durationArg.get(context);
        if (duration == null || duration <= 0) {
            context.sendMessage(Message.raw("--duration must be a positive number of seconds"));
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef target = context.provided(playerArg) ? playerArg.get(context) : null;
        World world = target != null
                ? Universe.get().getWorld(target.getWorldUuid())
                : this.worldArg.getProcessed(context);
        if (world == null) {
            context.sendMessage(Message.raw(target != null
                    ? "could not resolve the target player's world"
                    : "specify --world=<name>"));
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = world.getEntityStore().getStore();
        int seconds = duration;
        return this.runAsync(context, () -> apply(context, world, store, target, seconds), world);
    }

    private void apply(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store,
            PlayerRef target, int seconds) {
        long now = store.getResource(TimeResource.getResourceType()).getNow().toEpochMilli();
        long expiry = now + seconds * 1000L;
        GateStateResource gate = store.getResource(GateStateResource.getResourceType());

        if (target != null) {
            gate.timeoutPlayer(target.getUuid(), expiry);
            context.sendMessage(Message.raw(
                    "hexcode: timed out " + target.getUsername() + " for " + seconds + "s"));
        } else {
            gate.timeoutGlobal(expiry);
            store.getResource(HexExecutionQueue.getResourceType()).clear();
            context.sendMessage(Message.raw(
                    "hexcode: timed out all spellcasting in world " + world.getName() + " for " + seconds + "s"));
        }
    }
}
