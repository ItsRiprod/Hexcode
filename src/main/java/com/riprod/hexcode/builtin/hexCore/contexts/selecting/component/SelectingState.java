package com.riprod.hexcode.builtin.hexCore.contexts.selecting.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SelectingState implements Component<EntityStore> {

    public static final String CONTEXT_ID = "selecting";
    public static final int PRIORITY = 5;

    private static ComponentType<EntityStore, SelectingState> componentType;

    public static void setComponentType(ComponentType<EntityStore, SelectingState> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, SelectingState> getComponentType() {
        return componentType;
    }

    public SelectingState() {
    }

    @Nonnull
    @Override
    public SelectingState clone() {
        return new SelectingState();
    }
}
