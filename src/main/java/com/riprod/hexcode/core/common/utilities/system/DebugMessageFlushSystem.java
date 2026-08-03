package com.riprod.hexcode.core.common.utilities.system;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.utilities.resource.DebugMessageQueue;

public class DebugMessageFlushSystem extends TickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
        DebugMessageQueue queue = store.getResource(DebugMessageQueue.getResourceType());
        if (queue.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<Ref<EntityStore>, DebugMessageQueue.Entry>> it = queue.getEntries().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Ref<EntityStore>, DebugMessageQueue.Entry> mapEntry = it.next();
            DebugMessageQueue.Entry entry = mapEntry.getValue();
            if (!entry.isReady(dt)) {
                continue;
            }

            it.remove();

            Ref<EntityStore> ref = mapEntry.getKey();
            if (ref == null || !ref.isValid()) {
                continue;
            }

            try {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) {
                    continue;
                }
                playerRef.sendMessage(compose(entry));
            } catch (Exception e) {
                LOGGER.atSevere().log("[hexcode] DebugMessageFlushSystem failed: %s", e.getMessage());
            }
        }
    }

    private static Message compose(DebugMessageQueue.Entry entry) {
        Message composite = Message.raw("");
        boolean first = true;

        int dropped = entry.getDropped();
        if (dropped > 0) {
            composite.insert(markup(Message.translation("hexcode.debugGlyph.dropped").param("dropped", dropped)));
            first = false;
        }

        for (Message message : entry.getPending()) {
            if (!first) {
                composite.insert("\n");
            }
            composite.insert(message);
            first = false;
        }
        return composite;
    }

    private static Message markup(Message message) {
        message.getFormattedMessage().markupEnabled = true;
        return message;
    }
}
