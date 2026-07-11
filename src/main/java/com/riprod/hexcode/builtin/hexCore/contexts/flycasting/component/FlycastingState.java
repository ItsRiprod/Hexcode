package com.riprod.hexcode.builtin.hexCore.contexts.flycasting.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;

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
    private HexComponent hoveredHex;
    private GlyphComponent hoveredGlyph;
    private HexComponent lastHoveredHex;
    private HexComponent draggingHex;

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
    public HexComponent getHoveredHex() {
        return hoveredHex;
    }

    public void setHoveredHex(@Nullable HexComponent hoveredHex) {
        this.hoveredHex = hoveredHex;
    }

    @Nullable
    public GlyphComponent getHoveredGlyph() {
        return hoveredGlyph;
    }

    public void setHoveredGlyph(@Nullable GlyphComponent hoveredGlyph) {
        this.hoveredGlyph = hoveredGlyph;
    }

    @Nullable
    public HexComponent getLastHoveredHex() {
        return lastHoveredHex;
    }

    public void setLastHoveredHex(@Nullable HexComponent lastHoveredHex) {
        this.lastHoveredHex = lastHoveredHex;
    }

    @Nullable
    public HexComponent getDraggingHex() {
        return draggingHex;
    }

    public void setDraggingHex(@Nullable HexComponent draggingHex) {
        if (this.draggingHex != null) {
            this.draggingHex.setDragState(false);
        }
        this.draggingHex = draggingHex;
        if (draggingHex != null) {
            draggingHex.setDragState(true);
        }
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
        return copy;
    }
}
