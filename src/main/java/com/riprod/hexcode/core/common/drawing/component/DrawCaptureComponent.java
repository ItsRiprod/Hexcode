package com.riprod.hexcode.core.common.drawing.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class DrawCaptureComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, DrawCaptureComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, DrawCaptureComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, DrawCaptureComponent> getComponentType() {
        return componentType;
    }

    private final FloatArrayList strokePoints = new FloatArrayList();
    private boolean strokeActive;
    private long strokeStartMillis;
    private Ref<EntityStore> drawTrailRef;

    private final List<DrawnShapeComponent> pendingShapes = new ArrayList<>();
    private boolean finalizePending;
    private float finalizeTimer;
    private float finalizeDelaySeconds;

    private List<Hex> palette = new ArrayList<>();

    private HexComponent hoveredHex;
    private HexComponent draggingHex;
    private boolean dragReleaseRequested;

    public DrawCaptureComponent() {
    }

    public FloatArrayList getStrokePoints() {
        return strokePoints;
    }

    public boolean isStrokeActive() {
        return strokeActive;
    }

    public void setStrokeActive(boolean strokeActive) {
        this.strokeActive = strokeActive;
    }

    public long getStrokeStartMillis() {
        return strokeStartMillis;
    }

    public void setStrokeStartMillis(long strokeStartMillis) {
        this.strokeStartMillis = strokeStartMillis;
    }

    @Nullable
    public Ref<EntityStore> getDrawTrailRef() {
        return drawTrailRef;
    }

    public void setDrawTrailRef(@Nullable Ref<EntityStore> drawTrailRef) {
        this.drawTrailRef = drawTrailRef;
    }

    public List<DrawnShapeComponent> getPendingShapes() {
        return pendingShapes;
    }

    public boolean isFinalizePending() {
        return finalizePending;
    }

    public void setFinalizePending(boolean finalizePending) {
        this.finalizePending = finalizePending;
    }

    public float getFinalizeTimer() {
        return finalizeTimer;
    }

    public void setFinalizeTimer(float finalizeTimer) {
        this.finalizeTimer = finalizeTimer;
    }

    public float getFinalizeDelaySeconds() {
        return finalizeDelaySeconds;
    }

    public void setFinalizeDelaySeconds(float finalizeDelaySeconds) {
        this.finalizeDelaySeconds = finalizeDelaySeconds;
    }

    public List<Hex> getPalette() {
        return palette;
    }

    public void setPalette(List<Hex> palette) {
        this.palette = palette != null ? palette : new ArrayList<>();
    }

    @Nullable
    public HexComponent getHoveredHex() {
        return hoveredHex;
    }

    public void setHoveredHex(@Nullable HexComponent hoveredHex) {
        this.hoveredHex = hoveredHex;
    }

    @Nullable
    public HexComponent getDraggingHex() {
        return draggingHex;
    }

    public void setDraggingHex(@Nullable HexComponent draggingHex) {
        this.draggingHex = draggingHex;
    }

    public boolean consumeDragReleaseRequested() {
        boolean requested = this.dragReleaseRequested;
        this.dragReleaseRequested = false;
        return requested;
    }

    public void requestDragRelease() {
        this.dragReleaseRequested = true;
    }

    @Nonnull
    @Override
    public DrawCaptureComponent clone() {
        DrawCaptureComponent copy = new DrawCaptureComponent();
        copy.strokePoints.addAll(this.strokePoints);
        copy.strokeActive = this.strokeActive;
        copy.strokeStartMillis = this.strokeStartMillis;
        copy.drawTrailRef = this.drawTrailRef;
        copy.pendingShapes.addAll(this.pendingShapes);
        copy.finalizePending = this.finalizePending;
        copy.finalizeTimer = this.finalizeTimer;
        copy.finalizeDelaySeconds = this.finalizeDelaySeconds;
        copy.palette = new ArrayList<>(this.palette);
        copy.hoveredHex = this.hoveredHex;
        copy.draggingHex = this.draggingHex;
        copy.dragReleaseRequested = this.dragReleaseRequested;
        return copy;
    }
}
