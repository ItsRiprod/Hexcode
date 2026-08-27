package com.riprod.hexcode.core.common.construct.component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HexEffectsComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, HexEffectsComponent> componentType;

    private Map<UUID, HexStatus<?>> activeEffects = new HashMap<>();

    public HexEffectsComponent() {

    }

    public static void setComponentType(ComponentType<EntityStore, HexEffectsComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexEffectsComponent> getComponentType() {
        return componentType;
    }

    public void addEffect(UUID id, HexStatus<?> status) {
        activeEffects.put(id, status);
    }

    public void removeEffect(UUID id) {
        activeEffects.remove(id);
    }

    public Map<UUID, HexStatus<?>> getEffects() {
        return activeEffects;
    }

    @Nonnull
    @Override
    public HexEffectsComponent clone() {
        return new HexEffectsComponent();
    }
}
