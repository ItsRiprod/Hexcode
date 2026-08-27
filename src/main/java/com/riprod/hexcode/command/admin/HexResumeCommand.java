package com.riprod.hexcode.command.admin;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.resource.GateStateResource;

public class HexResumeCommand extends AbstractAsyncCommand {

    @Nonnull
    private final OptionalArg<PlayerRef> playerArg =
            this.withOptionalArg("player", "server.hexcode.commands.resume.player.desc", ArgTypes.PLAYER_REF);

    @Nonnull
    private final OptionalArg<World> worldArg =
            this.withOptionalArg("world", "server.hexcode.commands.resume.world.desc", ArgTypes.WORLD);

    public HexResumeCommand() {
        super("resume", "server.hexcode.commands.resume.desc");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
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
        return this.runAsync(context, () -> apply(context, world, store, target), world);
    }

    private void apply(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store,
            PlayerRef target) {
        GateStateResource gate = store.getResource(GateStateResource.getResourceType());
        if (target != null) {
            gate.resumePlayer(target.getUuid());
            context.sendMessage(Message.raw("hexcode: resumed spellcasting for " + target.getUsername()));
        } else {
            gate.resumeGlobal();
            context.sendMessage(Message.raw("hexcode: resumed spellcasting in world " + world.getName()));
        }
    }
}
