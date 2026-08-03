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
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.api.dispatch.ShapeStructure;

public class ShapeDrawnDiagnosticListener extends EntityEventSystem<EntityStore, ShapeDrawnEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ShapeDrawnDiagnosticListener() {
        super(ShapeDrawnEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ShapeDrawnEvent event) {
        ShapeStructure structure = event.getStructure();
        LOGGER.atFine().log("[event] ShapeDrawn player=%s shapes=%d volatility=%.3f efficiency=%.3f cancelled=%s",
                event.getPlayer(), structure.getShapes().size(), structure.getVolatility(),
                structure.getEfficiency(), event.isCancelled());
    }
}
