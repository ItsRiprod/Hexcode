package com.riprod.hexcode.builtin.hexCore.contexts.crafting.system;

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
import com.riprod.hexcode.api.dispatch.ShapeDrawnEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphResolver;
import com.riprod.hexcode.utils.LogScopes;

public class CraftingShapeDrawnSystem extends EntityEventSystem<EntityStore, ShapeDrawnEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.CRAFT);

    public CraftingShapeDrawnSystem() {
        super(ShapeDrawnEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull ShapeDrawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        var resolution = GlyphResolver.resolve(buffer, player, event.getStructure(),
                CraftingState.CONTEXT_ID);
        if (resolution.status() == GlyphResolver.Status.NO_MATCH) {
            LOGGER.atFine().log("no matching glyph found for drawn shape");
            return;
        }
        if (!resolution.isResolved()) {
            return;
        }
        buffer.invoke(player, new GlyphPlaceEvent(player, resolution.glyph(), resolution.asset(),
                event.getStructure()));
    }
}
