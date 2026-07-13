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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.gate.GateStateResource;
import com.riprod.hexcode.core.common.execution.queue.HexExecutionQueue;

public class HexTimeoutCommand extends AbstractAsyncCommand {

    @Nonnull
    private final OptionalArg<Integer> durationArg =
            this.withOptionalArg("duration", "timeout length in seconds", ArgTypes.INTEGER);

    @Nonnull
    private final OptionalArg<PlayerRef> playerArg =
            this.withOptionalArg("player", "player to time out (omit for the whole world)", ArgTypes.PLAYER_REF);

    @Nonnull
    private final OptionalArg<World> worldArg =
            this.withOptionalArg("world", "world to time out (defaults to your world; ignored when --player is given)", ArgTypes.WORLD);

    public HexTimeoutCommand() {
        super("timeout", "time out spellcasting for a player or the whole world");
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
