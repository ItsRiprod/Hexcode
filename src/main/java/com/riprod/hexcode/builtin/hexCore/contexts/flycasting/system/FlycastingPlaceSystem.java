package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.system;

import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.GlyphPlaceEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component.FlycastingState;
import com.riprod.hexcode.builtin.hexCore.contexts.flycasting.utils.FlycastingCommit;

public class FlycastingPlaceSystem extends EntityEventSystem<EntityStore, GlyphPlaceEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public FlycastingPlaceSystem() {
        super(GlyphPlaceEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return FlycastingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull GlyphPlaceEvent event) {
        try {
            FlycastingState state = chunk.getComponent(index, FlycastingState.getComponentType());
            if (state == null) {
                return;
            }
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            var structure = event.getStructure();
            FlycastingCommit.emitGlyphDrawn(player, event.getGlyph(),
                    structure != null ? structure.getShapes() : List.of(), event.getAsset());
            FlycastingCommit.spawnInAirHex(buffer, player, state, event.getGlyph());
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] flycasting glyph place failed");
        }
    }
}
