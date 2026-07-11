package com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MagicHealthComponent implements Component<EntityStore> {

    public static final String STAT_ID = "Magic_Health";

    private static ComponentType<EntityStore, MagicHealthComponent> componentType;

    private String effectId;

    public MagicHealthComponent() {
    }

    public MagicHealthComponent(String effectId) {
        this.effectId = effectId;
    }

    public static void setComponentType(ComponentType<EntityStore, MagicHealthComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, MagicHealthComponent> getComponentType() {
        return componentType;
    }

    public String getEffectId() {
        return effectId;
    }

    @Nonnull
    @Override
    public MagicHealthComponent clone() {
        return new MagicHealthComponent(effectId);
    }
}
