package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class FlycastingState implements Component<EntityStore> {

    public static final String CONTEXT_ID = "flycasting";
    public static final int PRIORITY = 1;

    private static ComponentType<EntityStore, FlycastingState> componentType;

    public static void setComponentType(ComponentType<EntityStore, FlycastingState> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, FlycastingState> getComponentType() {
        return componentType;
    }

    private Ref<EntityStore> castingRootRef;
    private List<Ref<EntityStore>> activeHexes = new ArrayList<>();
    private Ref<EntityStore> hoveredHex;
    private Ref<EntityStore> hoveredGlyph;
    private Ref<EntityStore> lastHoveredHex;
    private Ref<EntityStore> draggingHex;
    private String layoutId;

    public FlycastingState() {
    }

    @Nullable
    public Ref<EntityStore> getCastingRootRef() {
        return castingRootRef;
    }

    public void setCastingRootRef(@Nullable Ref<EntityStore> castingRootRef) {
        this.castingRootRef = castingRootRef;
    }

    public List<Ref<EntityStore>> getActiveHexes() {
        return activeHexes;
    }

    public void setActiveHexes(List<Ref<EntityStore>> activeHexes) {
        this.activeHexes = activeHexes != null ? activeHexes : new ArrayList<>();
    }

    @Nullable
    public Ref<EntityStore> getHoveredHex() {
        return hoveredHex;
    }

    public void setHoveredHex(@Nullable Ref<EntityStore> hoveredHex) {
        this.hoveredHex = hoveredHex;
    }

    @Nullable
    public Ref<EntityStore> getHoveredGlyph() {
        return hoveredGlyph;
    }

    public void setHoveredGlyph(@Nullable Ref<EntityStore> hoveredGlyph) {
        this.hoveredGlyph = hoveredGlyph;
    }

    @Nullable
    public Ref<EntityStore> getLastHoveredHex() {
        return lastHoveredHex;
    }

    public void setLastHoveredHex(@Nullable Ref<EntityStore> lastHoveredHex) {
        this.lastHoveredHex = lastHoveredHex;
    }

    @Nullable
    public Ref<EntityStore> getDraggingHex() {
        return draggingHex;
    }

    public void setDraggingHex(@Nullable Ref<EntityStore> draggingHex) {
        this.draggingHex = draggingHex;
    }

    public String getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
    }

    @Nonnull
    @Override
    public FlycastingState clone() {
        FlycastingState copy = new FlycastingState();
        copy.castingRootRef = this.castingRootRef;
        copy.activeHexes = new ArrayList<>(this.activeHexes);
        copy.hoveredHex = this.hoveredHex;
        copy.hoveredGlyph = this.hoveredGlyph;
        copy.lastHoveredHex = this.lastHoveredHex;
        copy.draggingHex = this.draggingHex;
        copy.layoutId = this.layoutId;
        return copy;
    }
}
