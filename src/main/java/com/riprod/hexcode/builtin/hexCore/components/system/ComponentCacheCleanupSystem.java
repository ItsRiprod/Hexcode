package com.riprod.hexcode.builtin.hexCore.components.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeExitEvent;
import com.riprod.hexcode.builtin.hexCore.components.component.ComponentPasteCache;

public class ComponentCacheCleanupSystem extends EntityEventSystem<EntityStore, DrawModeExitEvent> {

    public ComponentCacheCleanupSystem() {
        super(DrawModeExitEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return ComponentPasteCache.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull DrawModeExitEvent event) {
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        buffer.tryRemoveComponent(player, ComponentPasteCache.getComponentType());
    }
}
