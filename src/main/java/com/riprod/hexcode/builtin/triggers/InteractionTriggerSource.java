package com.riprod.hexcode.builtin.triggers;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedHotbarMarker;
import com.riprod.hexcode.core.common.triggers.component.FireTriggerEvent;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;

public final class InteractionTriggerSource {

    private InteractionTriggerSource() {
    }

    public static void register() {
        PacketAdapters.registerInbound((PlayerPacketWatcher) (playerRef, packet) -> {
            if (!(packet instanceof SyncInteractionChains chains)) return;
            for (SyncInteractionChain chain : chains.updates) {
                handleChain(playerRef, chain);
            }
        });
    }

    private static void handleChain(PlayerRef playerRef, SyncInteractionChain chain) {
        if (!chain.initial) return;
        String key = keyFor(chain.interactionType);
        if (key == null) return;

        Ref<EntityStore> playerEntityRef = playerRef.getReference();
        if (playerEntityRef == null || !playerEntityRef.isValid()) return;

        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world == null) return;

        int hotbarSlot = chain.activeHotbarSlot;
        InteractionType type = chain.interactionType;

        world.execute(() -> {
            Store<EntityStore> store = playerEntityRef.getStore();
            if (store.getComponent(playerEntityRef, ImbuedHotbarMarker.getComponentType()) == null) return;
            UUIDComponent uuidComp = store.getComponent(playerEntityRef, UUIDComponent.getComponentType());
            if (uuidComp == null) return;
            InteractionPayload payload = new InteractionPayload(playerEntityRef, type, null, hotbarSlot);
            store.invoke(new FireTriggerEvent(new TriggerEvent(key, uuidComp.getUuid(), playerEntityRef, payload)));
        });
    }

    @Nullable
    private static String keyFor(InteractionType type) {
        return switch (type) {
            case Primary -> TriggerKey.PRIMARY;
            case Secondary -> TriggerKey.SECONDARY;
            case Use -> TriggerKey.USE;
            case Ability1 -> Ability1Trigger.ID;
            case Ability2 -> Ability2Trigger.ID;
            case Ability3 -> Ability3Trigger.ID;
            default -> null;
        };
    }
}
