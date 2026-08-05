package com.riprod.hexcode.builtin.counterspell.eventListeners;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeExitEvent;
import com.riprod.hexcode.utils.LogScopes;

public class DrawModeExitDiagnosticListener extends EntityEventSystem<EntityStore, DrawModeExitEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    public DrawModeExitDiagnosticListener() {
        super(DrawModeExitEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull DrawModeExitEvent event) {
        LOGGER.atFine().log("[event] DrawModeExit player=%s", event.getPlayer());
    }
}
