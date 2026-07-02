package com.riprod.hexcode.core.common.node.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.node.NodeTypeId;

public class NodeComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, NodeComponent> componentType;

    public NodeComponent() {
    }

    public NodeComponent(NodeComponent other) {
        this.outgoingRefs = new ArrayList<>(other.outgoingRefs);
        this.incomingRefs = new ArrayList<>(other.incomingRefs);
        this.parentEntity = other.parentEntity;
        this.isHovered = other.isHovered;
        this.nodeType = other.nodeType;
    }

    public NodeComponent(Ref<EntityStore> parentEntity, NodeTypeId nodeType) {
        this.parentEntity = parentEntity;
        this.nodeType = nodeType;
    }

    public static void setComponentType(ComponentType<EntityStore, NodeComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, NodeComponent> getComponentType() {
        return componentType;
    }

    private List<Ref<EntityStore>> outgoingRefs = new ArrayList<>(); // links going away from this node
    private List<Ref<EntityStore>> incomingRefs = new ArrayList<>(); // links coming into this node
    private Ref<EntityStore> parentEntity; // parent object
    private boolean isHovered = false;
    private NodeTypeId nodeType;

    public List<Ref<EntityStore>> getOutgoingRefs() {
        return outgoingRefs;
    }

    public void setOutgoingRefs(List<Ref<EntityStore>> refs) {
        this.outgoingRefs = refs;
    }

    public void addOutgoingRef(Ref<EntityStore> ref) {
        this.outgoingRefs.add(ref);
    }

    public void removeOutgoingRef(Ref<EntityStore> nodeRef) {
        this.outgoingRefs.remove(nodeRef);
    }

    public List<Ref<EntityStore>> getIncomingRefs() {
        return incomingRefs;
    }

    public void setIncomingRefs(List<Ref<EntityStore>> refs) {
        this.incomingRefs = refs;
    }

    public void addIncomingRef(Ref<EntityStore> ref) {
        this.incomingRefs.add(ref);
    }

    public void removeIncomingRef(Ref<EntityStore> ref) {
        this.incomingRefs.remove(ref);
    }

    public boolean getIsHovered() {
        return isHovered;
    }

    public void setIsHovered(boolean hoverState) {
        this.isHovered = hoverState;
    }

    public Ref<EntityStore> getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(Ref<EntityStore> parentGlyphRef) {
        this.parentEntity = parentGlyphRef;
    }

    public NodeTypeId getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeTypeId nodeType) {
        this.nodeType = nodeType;
    }

    @Nonnull
    @Override
    public NodeComponent clone() {
        NodeComponent copy = new NodeComponent();
        copy.outgoingRefs = new ArrayList<>(this.outgoingRefs);
        copy.incomingRefs = new ArrayList<>(this.incomingRefs);
        copy.parentEntity = this.parentEntity;
        copy.isHovered = this.isHovered;
        copy.nodeType = this.nodeType;
        return copy;
    }
}
