package com.riprod.hexcode.builtin.hexCore.contexts.crafting.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CraftingState implements Component<EntityStore> {

    public static final String CONTEXT_ID = "crafting";
    public static final int PRIORITY = 10;

    private static ComponentType<EntityStore, CraftingState> componentType;

    public static void setComponentType(ComponentType<EntityStore, CraftingState> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, CraftingState> getComponentType() {
        return componentType;
    }

    public CraftingState() {
    }

    @Nonnull
    @Override
    public CraftingState clone() {
        return new CraftingState();
    }
}
