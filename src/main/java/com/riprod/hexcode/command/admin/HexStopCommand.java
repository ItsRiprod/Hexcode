package com.riprod.hexcode.command.admin;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.resource.GateStateResource;
import com.riprod.hexcode.core.common.execution.resource.HexExecutionQueue;

public class HexStopCommand extends AbstractWorldCommand {

    public HexStopCommand() {
        super("stop", "server.hexcode.commands.stop.desc");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull World world,
            @Nonnull Store<EntityStore> store) {
        store.getResource(GateStateResource.getResourceType()).stopGlobal();
        store.getResource(HexExecutionQueue.getResourceType()).clear();
        context.sendMessage(Message.raw("hexcode: all spellcasting stopped in world " + world.getName()));
    }
}
