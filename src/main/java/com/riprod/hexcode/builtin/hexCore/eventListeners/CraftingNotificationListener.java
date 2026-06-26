package com.riprod.hexcode.builtin.hexCore.eventListeners;

import java.util.function.Consumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.api.event.CraftingEvent;

public class CraftingNotificationListener implements Consumer<CraftingEvent> {

    @Override
    public void accept(CraftingEvent event) {
        CraftingEvent.Reason reason = event.getReason();
        if (!reason.isDenial() && !reason.isError()) return;

        Ref<EntityStore> playerRef = event.getPlayerRef();
        if (playerRef == null || !playerRef.isValid()) return;

        Store<EntityStore> store = playerRef.getStore();
        PlayerRef pr = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (pr == null) return;

        String title = resolveTitle(reason);
        String description = event.getMessage() != null ? event.getMessage() : resolveFallback(reason);

        NotificationUtil.sendNotification(pr.getPacketHandler(), title, description);
    }

    private static String resolveTitle(CraftingEvent.Reason reason) {
        return switch (reason) {
            case DENIED_OUT_OF_RANGE -> "Out of Range";
            case DENIED_PEDESTAL_BUSY -> "Pedestal Busy";
            case ERROR_INVALID_HEX -> "Cannot Craft";
            default -> "Crafting";
        };
    }

    private static String resolveFallback(CraftingEvent.Reason reason) {
        return switch (reason) {
            case DENIED_OUT_OF_RANGE -> "You're outside the pedestal's range.";
            case DENIED_PEDESTAL_BUSY -> "This pedestal is in use.";
            case ERROR_INVALID_HEX -> "No hex slot is active right now.";
            default -> "";
        };
    }
}
