package com.riprod.hexcode.builtin.hexCore.common;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.context.CasterComponent;

public final class ContextForceExitSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ContextForceExitSystem() {
    }

    public static class OnDeath extends RefChangeSystem<EntityStore, DeathComponent> {

        @Nonnull
        @Override
        public ComponentType<EntityStore, DeathComponent> componentType() {
            return DeathComponent.getComponentType();
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return DeathComponent.getComponentType();
        }

        @Override
        public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
                @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
            CasterComponent caster = store.getComponent(ref, CasterComponent.getComponentType());
            if (caster == null || caster.getCurrentContext() == null) {
                return;
            }
            buffer.invoke(ref, new ContextForceExitEvent(ref));
        }

        @Override
        public void onComponentSet(@Nonnull Ref<EntityStore> ref, DeathComponent oldComponent,
                @Nonnull DeathComponent newComponent, @Nonnull Store<EntityStore> store,
                @Nonnull CommandBuffer<EntityStore> buffer) {
        }

        @Override
        public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
                @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        }
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            PlayerRef playerRef = event.getPlayerRef();
            Ref<EntityStore> ref = playerRef != null ? playerRef.getReference() : null;
            if (ref == null) {
                return;
            }
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
                try {
                    if (!ref.isValid()) {
                        return;
                    }
                    CasterComponent caster = store.getComponent(ref, CasterComponent.getComponentType());
                    if (caster == null || caster.getCurrentContext() == null) {
                        return;
                    }
                    store.invoke(ref, new ContextForceExitEvent(ref));
                } catch (Exception e) {
                    LOGGER.atSevere().log("[hexcode] force-exit on disconnect failed: %s", e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] force-exit dispatch failed: %s", e.getMessage());
        }
    }
}
