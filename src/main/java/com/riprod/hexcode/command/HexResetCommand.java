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
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.core.state.casting.component.HexcasterCastingComponent;
import com.riprod.hexcode.core.state.crafting.component.HexcasterCraftingComponent;
import com.riprod.hexcode.core.state.drawing.component.HexcasterDrawingComponent;
import com.riprod.hexcode.core.state.drawing.system.InterfaceManager;
import com.riprod.hexcode.state.HexState;

public class HexResetCommand extends AbstractPlayerCommand {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexResetCommand() {
        super("reset", "Force reset hexcode state to IDLE");
        addAliases("r");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        try {
            HexcasterComponent hexcaster = store.getComponent(ref, HexcasterComponent.getComponentType());

            if (hexcaster == null) {
                send(playerRef, "no hexcaster component found");
                return;
            }

            HexState currentState = hexcaster.getState();
            hexcaster.consumePendingState();

            int cleaned = cleanupAll(store, ref, hexcaster);

            hexcaster.applyState(HexState.IDLE);

            if (currentState == HexState.IDLE && cleaned == 0) {
                send(playerRef, "already idle, nothing to reset");
            } else if (currentState == HexState.IDLE) {
                send(playerRef, "already idle, cleaned %d orphaned components", cleaned);
            } else {
                send(playerRef, "reset from %s -> IDLE (cleaned %d components)", currentState, cleaned);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("reset command failed: %s", e.getMessage());
            send(playerRef, "reset failed: %s", e.getMessage());
        }
    }

    private int cleanupAll(Store<EntityStore> store, Ref<EntityStore> ref, HexcasterComponent hexcaster) {
        int cleaned = 0;

        HexcasterDrawingComponent drawing = store.getComponent(ref, HexcasterDrawingComponent.getComponentType());
        if (drawing != null) {
            try { InterfaceManager.removeTrails(store, ref); } catch (Exception ignored) {}
            tryRemoveComponent(store, ref, HexcasterDrawingComponent.getComponentType());
            cleaned++;
        }

        HexcasterCastingComponent casting = store.getComponent(ref, HexcasterCastingComponent.getComponentType());
        if (casting != null) {
            safeRemoveRef(store, casting.getHeadAnchorRef());
            safeRemoveRef(store, casting.getCastingRootRef());
            for (Ref<EntityStore> hex : casting.getActiveHexes()) {
                safeRemoveRef(store, hex);
            }
            casting.clearCastingState();
            tryRemoveComponent(store, ref, HexcasterCastingComponent.getComponentType());
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
            if (mm == null) return;
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            if (pr == null) return;
            PacketHandler handler = pr.getPacketHandler();
            handler.writeNoCache(new SetMovementStates(new SavedMovementStates(false)));
            mm.applyDefaultSettings();
            handler.writeNoCache(new UpdateMovementSettings(mm.getSettings()));
        } catch (Exception ignored) {}
    }

    private void safeRemoveRef(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        if (entityRef == null || !entityRef.isValid()) return;
        try {
            store.removeEntity(entityRef, RemoveReason.REMOVE);
        } catch (Exception ignored) {}
    }

    private <T extends Component<EntityStore>> void tryRemoveComponent(
            Store<EntityStore> store, Ref<EntityStore> ref,
            ComponentType<EntityStore, T> type) {
        try {
            store.removeComponent(ref, type);
        } catch (Exception ignored) {}
    }

    private void send(PlayerRef playerRef, String message, Object... args) {
        String formatted = args.length > 0 ? String.format(message, args) : message;
        playerRef.sendMessage(Message.raw(formatted));
        LOGGER.atInfo().log(formatted);
    }
}
