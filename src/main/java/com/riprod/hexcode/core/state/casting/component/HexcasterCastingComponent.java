package com.riprod.hexcode.core.state.casting.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.GlyphComponent;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.state.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.utils.CleanupUtils;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class HexcasterCastingComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, HexcasterCastingComponent> componentType;

    private Ref<EntityStore> castingRootRef = null;
    private Ref<EntityStore> headAnchorRef = null;
    @Nonnull
    private List<Ref<EntityStore>> activeHexes = new ArrayList<>();
    private Ref<EntityStore> hoveredChain = null;
    private HexComponent draggingHex = null;
    private HexComponent hoveredHex = null;
    private GlyphComponent hoveredGlyph = null;
    private HexComponent lastHoveredHex = null;

    public enum DraftSubState { Idle, Drawing, AwaitingFinalize }

    private transient DraftSubState draftSubState = DraftSubState.Idle;
    private final transient FloatArrayList currentStrokePoints = new FloatArrayList();
    private final transient List<DrawnShapeComponent> pendingShapes = new ArrayList<>();
    private transient float finalizeTimer = 0f;
    private transient float finalizeDelaySeconds = 0f;
    private transient long strokeStartMillis = 0L;
    private transient Ref<EntityStore> drawTrailRef = null;

    public HexcasterCastingComponent() {
    }

    public static void setComponentType(ComponentType<EntityStore, HexcasterCastingComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexcasterCastingComponent> getComponentType() {
        return componentType;
    }

    public HexComponent getHoveredHex() {
        return hoveredHex;
    }

    public void setHoveredHex(@Nullable HexComponent hoveredHex) {
        this.hoveredHex = hoveredHex;
    }

    public GlyphComponent getHoveredGlyph() {
        return hoveredGlyph;
    }

    public void setHoveredGlyph(@Nullable GlyphComponent hoveredGlyph) {
        this.hoveredGlyph = hoveredGlyph;
    }

    public HexComponent getDraggingHex() {
        return draggingHex;
    }

    public void setDraggingHex(@Nullable HexComponent draggingHex) {
        if (this.draggingHex != null) {
            this.draggingHex.setDragState(false);
        }

        if (draggingHex == null) {
            this.draggingHex = null;
            return;
        }

        this.draggingHex = draggingHex;
        this.draggingHex.setDragState(true);
    }

    public Ref<EntityStore> getHeadAnchorRef() {
        return headAnchorRef;
    }

    public void setHeadAnchorRef(@Nullable Ref<EntityStore> headAnchorRef) {
        this.headAnchorRef = headAnchorRef;
    }

    public Ref<EntityStore> getCastingRootRef() {
        return castingRootRef;
    }

    public Ref<EntityStore> setCastingRootRef(Ref<EntityStore> castingRootRef) {
        Ref<EntityStore> oldRef = this.castingRootRef;
        this.castingRootRef = castingRootRef;
        return oldRef;
    }

    public List<Ref<EntityStore>> getActiveHexes() {
        return activeHexes;
    }

    public void setActiveHexes(@Nonnull List<Ref<EntityStore>> activeHexes) {
        this.activeHexes = activeHexes;
    }

    public void setLastHoveredHex(HexComponent hex) {
        this.lastHoveredHex = hex;
    }

    public HexComponent getLastHoveredHex() {
        return this.lastHoveredHex;
    }

    public void clearCastingState() {
        this.castingRootRef = null;
        this.headAnchorRef = null;
        this.activeHexes.clear();
        this.hoveredChain = null;
        this.draggingHex = null;
        this.hoveredHex = null;
        this.hoveredGlyph = null;
        this.lastHoveredHex = null;
        this.draftSubState = DraftSubState.Idle;
        this.currentStrokePoints.clear();
        this.pendingShapes.clear();
        this.finalizeTimer = 0f;
        this.finalizeDelaySeconds = 0f;
        this.strokeStartMillis = 0L;
        this.drawTrailRef = null;
    }

    public DraftSubState getDraftSubState() {
        return draftSubState;
    }

    public void setDraftSubState(DraftSubState state) {
        this.draftSubState = state;
    }

    public FloatArrayList getCurrentStrokePoints() {
        return currentStrokePoints;
    }

    public void clearCurrentStroke() {
        this.currentStrokePoints.clear();
    }

    public List<DrawnShapeComponent> getPendingShapes() {
        return pendingShapes;
    }

    public void addPendingShape(DrawnShapeComponent shape) {
        this.pendingShapes.add(shape);
    }

    public void clearPendingShapes() {
        this.pendingShapes.clear();
    }

    public float getFinalizeTimer() {
        return finalizeTimer;
    }

    public void setFinalizeTimer(float timer) {
        this.finalizeTimer = timer;
    }

    public float getFinalizeDelaySeconds() {
        return finalizeDelaySeconds;
    }

    public void setFinalizeDelaySeconds(float seconds) {
        this.finalizeDelaySeconds = seconds;
    }

    public long getStrokeStartMillis() {
        return strokeStartMillis;
    }

    public void setStrokeStartMillis(long millis) {
        this.strokeStartMillis = millis;
    }

    @Nullable
    public Ref<EntityStore> getDrawTrailRef() {
        return drawTrailRef;
    }

    public void setDrawTrailRef(@Nullable Ref<EntityStore> ref) {
        this.drawTrailRef = ref;
    }

    public void clear(CommandBuffer<EntityStore> buffer) {
        this.castingRootRef = null;
        CleanupUtils.safeRemoveEntity(buffer, this.headAnchorRef);
        CleanupUtils.safeRemoveEntities(buffer, activeHexes);
        CleanupUtils.safeRemoveEntity(buffer, this.drawTrailRef);
        this.headAnchorRef = null;
        this.activeHexes.clear();
        this.hoveredChain = null;
        this.draggingHex = null;
        this.hoveredHex = null;
        this.hoveredGlyph = null;
        this.lastHoveredHex = null;
        this.draftSubState = DraftSubState.Idle;
        this.currentStrokePoints.clear();
        this.pendingShapes.clear();
        this.finalizeTimer = 0f;
        this.finalizeDelaySeconds = 0f;
        this.strokeStartMillis = 0L;
        this.drawTrailRef = null;
    }

    @Nonnull
    @Override
    public HexcasterCastingComponent clone() {
        HexcasterCastingComponent copy = new HexcasterCastingComponent();
        copy.castingRootRef = this.castingRootRef;
        copy.headAnchorRef = this.headAnchorRef;
        copy.activeHexes = new ArrayList<>(this.activeHexes);
        copy.hoveredChain = this.hoveredChain;
        copy.draggingHex = this.draggingHex;
        copy.hoveredHex = this.hoveredHex;
        copy.hoveredGlyph = this.hoveredGlyph;
        copy.lastHoveredHex = this.lastHoveredHex;
        return copy;
    }
}
