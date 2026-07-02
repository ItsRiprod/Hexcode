package com.riprod.hexcode.core.common.node.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SlotComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, SlotComponent> componentType;

    private String slotKey;

    public SlotComponent() {
    }

    public SlotComponent(String slotKey) {
        this.slotKey = slotKey;
    }

    public static void setComponentType(ComponentType<EntityStore, SlotComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, SlotComponent> getComponentType() {
        return componentType;
    }

    public String getSlotKey() {
        return slotKey;
    }

    @Nonnull
    @Override
    public SlotComponent clone() {
        SlotComponent copy = new SlotComponent();
        copy.slotKey = this.slotKey;
        return copy;
    }
}
