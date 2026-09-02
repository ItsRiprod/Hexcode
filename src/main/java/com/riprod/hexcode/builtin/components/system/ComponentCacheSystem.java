package com.riprod.hexcode.builtin.components.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.context.DrawModeEnterEvent;
import com.riprod.hexcode.builtin.components.component.ComponentPasteCache;
import com.riprod.hexcode.builtin.components.utils.ComponentScan;

public class ComponentCacheSystem extends WorldEventSystem<EntityStore, DrawModeEnterEvent> {

    public ComponentCacheSystem() {
        super(DrawModeEnterEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull DrawModeEnterEvent event) {
        Ref<EntityStore> player = event.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }
        var cache = new ComponentPasteCache();
        cache.setEntries(ComponentScan.scan(buffer, player));
        buffer.putComponent(player, ComponentPasteCache.getComponentType(), cache);
    }
}
