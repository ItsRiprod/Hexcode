package com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class FortifyWardComponent implements Component<EntityStore> {

    public static final FortifyWardComponent INSTANCE = new FortifyWardComponent();

    private static ComponentType<EntityStore, FortifyWardComponent> componentType;

    private FortifyWardComponent() {
    }

    public static void setComponentType(ComponentType<EntityStore, FortifyWardComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, FortifyWardComponent> getComponentType() {
        return componentType;
    }

    @Nonnull
    @Override
    public FortifyWardComponent clone() {
        return INSTANCE;
    }
}
