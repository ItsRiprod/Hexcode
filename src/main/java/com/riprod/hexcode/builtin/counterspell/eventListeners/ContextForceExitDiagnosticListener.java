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
import com.riprod.hexcode.builtin.hexCore.common.ContextForceExitEvent;
import com.riprod.hexcode.utils.LogScopes;

public class ContextForceExitDiagnosticListener extends EntityEventSystem<EntityStore, ContextForceExitEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DIAG);

    public ContextForceExitDiagnosticListener() {
        super(ContextForceExitEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ContextForceExitEvent event) {
        LOGGER.atFine().log("[event] ContextForceExit player=%s", event.getPlayer());
    }
}
