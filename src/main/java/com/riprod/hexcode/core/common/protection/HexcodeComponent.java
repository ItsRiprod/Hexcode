package com.riprod.hexcode.core.common.protection;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HexcodeComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, HexcodeComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, HexcodeComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexcodeComponent> getComponentType() {
        return componentType;
    }

    public HexcodeComponent() {
    }

    @Nonnull
    @Override
    public HexcodeComponent clone() {
        return new HexcodeComponent();
    }
}
