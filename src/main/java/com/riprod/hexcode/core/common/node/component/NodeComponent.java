package com.riprod.hexcode.core.common.node.component;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NodeComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, NodeComponent> componentType;

    private Ref<EntityStore> parentEntity;
    private String configId;

    public NodeComponent() {
    }

    public NodeComponent(Ref<EntityStore> parentEntity, String configId) {
        this.parentEntity = parentEntity;
        this.configId = configId;
    }

    public static void setComponentType(ComponentType<EntityStore, NodeComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, NodeComponent> getComponentType() {
        return componentType;
    }

    public Ref<EntityStore> getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(Ref<EntityStore> parentGlyphRef) {
        this.parentEntity = parentGlyphRef;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    @Nonnull
    @Override
    public NodeComponent clone() {
        return new NodeComponent(this.parentEntity, this.configId);
    }
}
