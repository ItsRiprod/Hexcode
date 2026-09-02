package com.riprod.hexcode.builtin.hexCore.contexts.decrypting.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DecryptingState implements Component<EntityStore> {

    public static final String CONTEXT_ID = "decrypting";
    public static final int PRIORITY = 5;

    private static ComponentType<EntityStore, DecryptingState> componentType;

    public static void setComponentType(ComponentType<EntityStore, DecryptingState> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, DecryptingState> getComponentType() {
        return componentType;
    }

    public DecryptingState() {
    }

    @Nonnull
    @Override
    public DecryptingState clone() {
        return new DecryptingState();
    }
}
