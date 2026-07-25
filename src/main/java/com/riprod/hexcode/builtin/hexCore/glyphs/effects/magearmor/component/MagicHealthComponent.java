package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MagicHealthComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, MagicHealthComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, MagicHealthComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, MagicHealthComponent> getComponentType() {
        return componentType;
    }

    @Nonnull
    @Override
    public MagicHealthComponent clone() {
        return new MagicHealthComponent();
    }
}
