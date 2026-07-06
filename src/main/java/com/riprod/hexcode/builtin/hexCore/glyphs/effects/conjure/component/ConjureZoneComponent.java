package com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ConjureZoneComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, ConjureZoneComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, ConjureZoneComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, ConjureZoneComponent> getComponentType() {
        return componentType;
    }

    private Vector3d halfExtents;
    private float interval;
    private float intervalTimer;
    private float duration;
    private float totallapsed = 0f;
    private Set<UUID> lastOccupants;
    private Set<UUID> newOccupants;
    private float spatialQueryTimer;
    private Ref<EntityStore> zoneRef;

    public ConjureZoneComponent() {
    }

    public ConjureZoneComponent(Vector3d halfExtents, float interval, float duration) {
        this.halfExtents = halfExtents;
        this.interval = interval;
        this.intervalTimer = interval;
        this.duration = duration;
        this.lastOccupants = new HashSet<>();
        this.newOccupants = new HashSet<>();
        this.spatialQueryTimer = 0;
    }

    public Vector3d getHalfExtents() {
        return halfExtents;
    }

    public float getInterval() {
        return interval;
    }

    public float getIntervalTimer() {
        return intervalTimer;
    }

    public void setIntervalTimer(float intervalTimer) {
        this.intervalTimer = intervalTimer;
    }

    public float getDuration() {
        return duration;
    }

    public float getTotallapsed() {
        return totallapsed;
    }

    public void addToTotallapsed(float delta) {
        this.totallapsed += delta;
    }

    public Set<UUID> getLastOccupants() {
        return lastOccupants;
    }

    public void setLastOccupants(Set<UUID> lastOccupants) {
        this.lastOccupants = lastOccupants;
    }

    public Set<UUID> getNewOccupants() {
        return newOccupants;
    }

    public void setNewOccupants(Set<UUID> newOccupants) {
        this.newOccupants = newOccupants;
    }

    public float getSpatialQueryTimer() {
        return spatialQueryTimer;
    }

    public void setSpatialQueryTimer(float spatialQueryTimer) {
        this.spatialQueryTimer = spatialQueryTimer;
    }

    public Ref<EntityStore> getZoneRef() {
        return zoneRef;
    }

    public void setZoneRef(Ref<EntityStore> zoneRef) {
        this.zoneRef = zoneRef;
    }

    @Nonnull
    @Override
    public ConjureZoneComponent clone() {
        ConjureZoneComponent copy = new ConjureZoneComponent();
        copy.halfExtents = this.halfExtents != null ? new Vector3d(this.halfExtents) : null;
        copy.interval = this.interval;
        copy.intervalTimer = this.intervalTimer;
        copy.lastOccupants = new HashSet<>();
        copy.newOccupants = new HashSet<>();
        copy.spatialQueryTimer = this.spatialQueryTimer;
        copy.zoneRef = this.zoneRef;
        return copy;
    }
}
