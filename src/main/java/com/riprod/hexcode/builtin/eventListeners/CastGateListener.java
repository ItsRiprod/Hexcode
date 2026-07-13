package com.riprod.hexcode.builtin.eventListeners;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.PlayerHexRoot;
import com.riprod.hexcode.core.common.execution.events.HexCastEventSystem;
import com.riprod.hexcode.core.common.execution.gate.GateStateResource;

public class CastGateListener extends WorldEventSystem<EntityStore, HexCastEvent> {

    public CastGateListener() {
        super(HexCastEvent.class);
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency<>(Order.BEFORE, HexCastEventSystem.class));
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> buffer,
                       @Nonnull HexCastEvent event) {
        HexContext data = event.getContext();
        if (!(data.getHexRoot() instanceof PlayerHexRoot root)) {
            return;
        }
        Ref<EntityStore> casterRef = root.getSourceRef();
        if (casterRef == null || !casterRef.isValid()) {
            return;
        }
        PlayerRef pr = buffer.getComponent(casterRef, PlayerRef.getComponentType());
        if (pr == null) {
            return;
        }
        UUID uuid = pr.getUuid();

        GateStateResource gate = store.getResource(GateStateResource.getResourceType());
        long now = store.getResource(TimeResource.getResourceType()).getNow().toEpochMilli();
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
