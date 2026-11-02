package com.riprod.hexcode.builtin.hexCore.eventListeners;

import java.util.UUID;
import java.util.function.Consumer;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.resource.GateStateResource;

public class CastGateListener implements Consumer<CastEvent> {

    @Override
    public void accept(CastEvent event) {
        HexContext data = event.getContext();
        CommandBuffer<EntityStore> buffer = data.getAccessor();
        Ref<EntityStore> casterRef = data.getRootRef();
        PlayerRef pr = buffer.getComponent(casterRef, PlayerRef.getComponentType());
        if (pr == null) {
            return;
        }
        UUID uuid = pr.getUuid();

        GateStateResource gate = buffer.getResource(GateStateResource.getResourceType());
        long now = buffer.getResource(TimeResource.getResourceType()).getNow().toEpochMilli();
        if (!gate.isCasterGated(uuid, now)) {
            return;
        }

        event.setCancelled(true);
        long expiry = gate.getExpiryFor(uuid);
        Message notification;
        if (expiry == GateStateResource.STOPPED) {
            notification = Message.translation("server.hexcode.notifications.castDisabled");
        } else {
            long remaining = Math.max(1L, (expiry - now + 999L) / 1000L);
            notification = Message.translation("server.hexcode.notifications.castTimeout").param("seconds", remaining);
        }
        NotificationUtil.sendNotification(pr.getPacketHandler(), notification);
    }
}
