package com.riprod.hexcode.core.common.execution.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class ExecutionComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, ExecutionComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, ExecutionComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, ExecutionComponent> getComponentType() {
        return componentType;
    }

    private Hex queuedHex;

    public ExecutionComponent() {
    }

    @Nullable
    public Hex getQueuedHex() {
        return queuedHex;
    }

    public void setQueuedHex(@Nullable Hex queuedHex) {
        this.queuedHex = queuedHex;
    }

    @Nonnull
    @Override
    public ExecutionComponent clone() {
        ExecutionComponent copy = new ExecutionComponent();
        copy.queuedHex = this.queuedHex;
        return copy;
    }
}
