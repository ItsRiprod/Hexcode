package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.FlycastingCommit;

public class FlycastingShapeDrawnSystem extends EntityEventSystem<EntityStore, ShapeDrawnEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public FlycastingShapeDrawnSystem() {
        super(ShapeDrawnEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return FlycastingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ShapeDrawnEvent event) {
        try {
            if (event.isCancelled()) {
                return;
            }
            FlycastingState state = chunk.getComponent(index, FlycastingState.getComponentType());
            if (state == null) {
                return;
            }
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            FlycastingCommit.commitShape(buffer, player, state, event.getStructure());
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] flycasting shape drawn failed");
        }
    }
}
