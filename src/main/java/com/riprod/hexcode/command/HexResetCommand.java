package com.riprod.hexcode.command;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.protocol.packets.player.UpdateMovementSettings;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.riprod.hexcode.builtin.hexCore.common.ContextForceExitEvent;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.component.HexcasterDrawingComponent;
import com.riprod.hexcode.core.common.drawing.system.InterfaceManager;
import com.riprod.hexcode.core.common.pedestal.component.HexcasterCraftingComponent;

public class HexResetCommand extends AbstractPlayerCommand {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexResetCommand() {
        super("reset", "Force exit any hexcode context and clean up");

        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);
        addAliases("r");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        try {
            CasterComponent caster = store.getComponent(ref, CasterComponent.getComponentType());
            String currentContext = caster != null ? caster.getCurrentContext() : null;

            if (currentContext != null) {
                store.invoke(ref, new ContextForceExitEvent(ref));
            }

            int cleaned = cleanupAll(store, ref);

            if (currentContext == null && cleaned == 0) {
                send(playerRef, "no active context, nothing to reset");
            } else if (currentContext == null) {
                send(playerRef, "no active context, cleaned %d orphaned components", cleaned);
            } else {
                send(playerRef, "force-exited %s (cleaned %d components)", currentContext, cleaned);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("reset command failed: %s", e.getMessage());
            send(playerRef, "reset failed: %s", e.getMessage());
        }
    }

    private int cleanupAll(Store<EntityStore> store, Ref<EntityStore> ref) {
        int cleaned = 0;

        DrawCaptureComponent capture = store.getComponent(ref, DrawCaptureComponent.getComponentType());
        if (capture != null) {
            safeRemoveRef(store, capture.getDrawTrailRef());
            tryRemoveComponent(store, ref, DrawCaptureComponent.getComponentType());
            cleaned++;
        }

        HexcasterDrawingComponent drawing = store.getComponent(ref, HexcasterDrawingComponent.getComponentType());
        if (drawing != null) {
            try {
                InterfaceManager.removeTrails(store, ref);
            } catch (Exception ignored) {
            }
            tryRemoveComponent(store, ref, HexcasterDrawingComponent.getComponentType());
            cleaned++;
        }

        HexcasterCraftingComponent crafting = store.getComponent(ref, HexcasterCraftingComponent.getComponentType());
        if (crafting != null) {
            tryRemoveComponent(store, ref, HexcasterCraftingComponent.getComponentType());
            resetFlight(store, ref);
            cleaned++;
        }

        return cleaned;
    }

    private void resetFlight(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            MovementManager mm = store.getComponent(ref, MovementManager.getComponentType());
            if (mm == null)
                return;
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            if (pr == null)
                return;
            PacketHandler handler = pr.getPacketHandler();
            handler.writeNoCache(new SetMovementStates(new SavedMovementStates(false)));
            mm.applyDefaultSettings();
            handler.writeNoCache(new UpdateMovementSettings(mm.getSettings()));
        } catch (Exception ignored) {
        }
    }

    private void safeRemoveRef(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid())
            return;
        try {
            store.removeEntity(entityRef, RemoveReason.REMOVE);
        } catch (Exception ignored) {
        }
    }

    private <T extends Component<EntityStore>> void tryRemoveComponent(
            Store<EntityStore> store, Ref<EntityStore> ref,
            ComponentType<EntityStore, T> type) {
        try {
            store.removeComponent(ref, type);
        } catch (Exception ignored) {
        }
    }

    private void send(PlayerRef playerRef, String message, Object... args) {
        String formatted = args.length > 0 ? String.format(message, args) : message;
        playerRef.sendMessage(Message.raw(formatted));
        LOGGER.atInfo().log(formatted);
    }
}
