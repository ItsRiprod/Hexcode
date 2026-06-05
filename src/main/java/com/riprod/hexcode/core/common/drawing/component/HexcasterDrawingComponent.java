package com.riprod.hexcode.core.common.drawing.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.utils.CleanupUtils;

import it.unimi.dsi.fastutil.floats.FloatArrayList;


public class HexcasterDrawingComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, HexcasterDrawingComponent> componentType;

    public HexcasterDrawingComponent() {
    }

    public static void setComponentType(ComponentType<EntityStore, HexcasterDrawingComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexcasterDrawingComponent> getComponentType() {
        return componentType;
    }

    private FloatArrayList drawnStrokes = new FloatArrayList();
    private List<DrawnShapeComponent> drawnGlyphs = new ArrayList<>();
    private Ref<EntityStore> trailRef = null;
    private long lastParticleSpawnMillis = 0;
    private long drawStartTimeMillis = 0;

    public void clearDrawingState() {
        this.drawnStrokes.clear();
        this.drawnGlyphs.clear();
        this.trailRef = null;
        this.lastParticleSpawnMillis = 0;
        this.drawStartTimeMillis = 0;
    }

    public FloatArrayList getDrawnStrokes() {
        return drawnStrokes;
    }

    public List<DrawnShapeComponent> getDrawnGlyphs() {
        return drawnGlyphs;
    }

    public void addDrawnStroke(float[] stroke) {
        this.drawnStrokes.add(stroke[0]);
        this.drawnStrokes.add(stroke[1]);
    }

    public void addDrawnGlyph(DrawnShapeComponent glyph) {
        this.drawnGlyphs.add(glyph);
    }

    public void clearStrokes() {
        this.drawnStrokes.clear();
    }

    public void clearDrawing() {
        clearStrokes();
        this.drawnGlyphs.clear();
    }

    public void setTrailRef(Ref<EntityStore> trailRef) {
        this.trailRef = trailRef;
    }

    public void clearTrailRef() {
        this.trailRef = null;
    }

    public Ref<EntityStore> getTrailRef() {
        return trailRef;
    }

    public Long getLastParticleSpawnMillis() {
        return lastParticleSpawnMillis;
    }

    public void setLastParticleSpawnMillis(Long millis) {
        this.lastParticleSpawnMillis = millis;
    }

    public long getDrawStartTimeMillis() {
        return drawStartTimeMillis;
    }

    public void setDrawStartTimeMillis(long drawStartTimeMillis) {
        this.drawStartTimeMillis = drawStartTimeMillis;
    }

    public void clear(CommandBuffer<EntityStore> buffer) {
        clearDrawingState();
        if (trailRef != null) {
            CleanupUtils.safeRemoveEntity(buffer, trailRef);
            trailRef = null;
        }
    }

    @Nonnull
    @Override
    public HexcasterDrawingComponent clone() {
        HexcasterDrawingComponent copy = new HexcasterDrawingComponent();
        copy.drawnStrokes = new FloatArrayList(this.drawnStrokes);
        copy.drawnGlyphs = new ArrayList<>(this.drawnGlyphs);
        copy.trailRef = this.trailRef;
        copy.lastParticleSpawnMillis = this.lastParticleSpawnMillis;
        copy.drawStartTimeMillis = this.drawStartTimeMillis;
        return copy;
    }
}

