package com.riprod.hexcode.builtin.hexCore.components.component;

import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ComponentPasteCache implements Component<EntityStore> {

    private static ComponentType<EntityStore, ComponentPasteCache> componentType;

    public static void setComponentType(ComponentType<EntityStore, ComponentPasteCache> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, ComponentPasteCache> getComponentType() {
        return componentType;
    }

    private List<ComponentCacheEntry> entries = List.of();

    public List<ComponentCacheEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ComponentCacheEntry> entries) {
        this.entries = entries != null ? entries : List.of();
    }

    @Nonnull
    @Override
    public ComponentPasteCache clone() {
        ComponentPasteCache copy = new ComponentPasteCache();
        copy.entries = List.copyOf(this.entries);
        return copy;
    }
}
