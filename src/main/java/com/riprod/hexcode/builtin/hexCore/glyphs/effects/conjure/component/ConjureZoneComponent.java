package com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ConjureZoneComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, ConjureZoneComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, ConjureZoneComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, ConjureZoneComponent> getComponentType() {
        return componentType;
    }

    private Vector3d baseHalfExtents;
    private float interval;
    private float intervalTimer;
    private float duration;
    private float totallapsed = 0f;
    private Set<UUID> lastOccupants;
    private Set<UUID> newOccupants;
    private float spatialQueryTimer;
    private Ref<EntityStore> zoneRef;

    private float cachedPitch;
    private float cachedYaw;
    private float cachedRoll;
    private float cachedScale = 1f;

    private Vector3d effectiveHalfExtents;
    private Vector3d aabbHalfExtents;
    private Quaterniond rotation;
    private Quaterniond inverseRotation;
    private boolean identityRotation = true;

    public ConjureZoneComponent() {
    }

    public ConjureZoneComponent(Vector3d baseHalfExtents, float interval, float duration) {
        this.baseHalfExtents = baseHalfExtents;
        this.interval = interval;
        this.intervalTimer = interval;
        this.duration = duration;
        this.lastOccupants = new HashSet<>();
        this.newOccupants = new HashSet<>();
        this.spatialQueryTimer = 0;
        project();
    }

    public boolean reproject(float pitch, float yaw, float roll, float scale) {
        if (baseHalfExtents == null) {
            return false;
        }
        if (effectiveHalfExtents != null
                && pitch == cachedPitch && yaw == cachedYaw && roll == cachedRoll
                && scale == cachedScale) {
            return false;
        }
        cachedPitch = pitch;
        cachedYaw = yaw;
        cachedRoll = roll;
        cachedScale = scale;
        project();
        return true;
    }

    private void project() {
        if (baseHalfExtents == null) {
            return;
        }
        effectiveHalfExtents = new Vector3d(baseHalfExtents).mul(cachedScale);
        identityRotation = cachedPitch == 0f && cachedYaw == 0f && cachedRoll == 0f;

        if (identityRotation) {
            rotation = null;
            inverseRotation = null;
            aabbHalfExtents = new Vector3d(effectiveHalfExtents);
            return;
        }

        rotation = new Quaterniond().rotationYXZ(cachedYaw, cachedPitch, cachedRoll);
        inverseRotation = new Quaterniond(rotation).conjugate();

        Matrix3d m = new Matrix3d().set(rotation);
        Vector3d e = effectiveHalfExtents;
        aabbHalfExtents = new Vector3d(
                Math.abs(m.m00()) * e.x + Math.abs(m.m10()) * e.y + Math.abs(m.m20()) * e.z,
                Math.abs(m.m01()) * e.x + Math.abs(m.m11()) * e.y + Math.abs(m.m21()) * e.z,
                Math.abs(m.m02()) * e.x + Math.abs(m.m12()) * e.y + Math.abs(m.m22()) * e.z);
    }

    public Vector3d getEffectiveHalfExtents() {
        return effectiveHalfExtents;
    }

    public Vector3d getAabbHalfExtents() {
        return aabbHalfExtents;
    }

    public Vector3d getDebugSize() {
        return new Vector3d(effectiveHalfExtents).mul(2.0);
    }

    public boolean containsPoint(Vector3dc center, Vector3dc point) {
        Vector3d e = effectiveHalfExtents;
        double dx = point.x() - center.x();
        double dy = point.y() - center.y();
        double dz = point.z() - center.z();

        if (!identityRotation) {
            Vector3d local = inverseRotation.transform(new Vector3d(dx, dy, dz));
            dx = local.x;
            dy = local.y;
            dz = local.z;
        }

        return Math.abs(dx) <= e.x && Math.abs(dy) <= e.y && Math.abs(dz) <= e.z;
    }

    public double computeEjection(Vector3dc center, Vector3dc point, Vector3dc halfExtents,
            int axisRank, Vector3d outDirection) {
        Vector3d e = effectiveHalfExtents;
        if (e == null || axisRank < 0 || axisRank > 2) {
            return 0;
        }

        double dx = point.x() - center.x();
        double dy = point.y() - center.y();
        double dz = point.z() - center.z();

        if (!identityRotation) {
            Vector3d local = inverseRotation.transform(new Vector3d(dx, dy, dz));
            dx = local.x;
            dy = local.y;
            dz = local.z;
        }

        double px = e.x + halfExtents.x() - Math.abs(dx);
        double py = e.y + halfExtents.y() - Math.abs(dy);
        double pz = e.z + halfExtents.z() - Math.abs(dz);
        if (px <= 0 || py <= 0 || pz <= 0) {
            return 0;
        }

        double[] depths = { px, py, pz };
        int[] order = { 0, 1, 2 };
        for (int i = 1; i < order.length; i++) {
            for (int j = i; j > 0 && depths[order[j]] < depths[order[j - 1]]; j--) {
                int swap = order[j];
                order[j] = order[j - 1];
                order[j - 1] = swap;
            }
        }

        int axis = order[axisRank];
        switch (axis) {
            case 0 -> outDirection.set(Math.copySign(1, dx), 0, 0);
            case 1 -> outDirection.set(0, Math.copySign(1, dy), 0);
            default -> outDirection.set(0, 0, Math.copySign(1, dz));
        }

        if (!identityRotation) {
            rotation.transform(outDirection);
        }

        return depths[axis];
    }

    public Vector3d getBaseHalfExtents() {
        return baseHalfExtents;
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
        copy.baseHalfExtents = this.baseHalfExtents != null ? new Vector3d(this.baseHalfExtents) : null;
        copy.interval = this.interval;
        copy.intervalTimer = this.intervalTimer;
        copy.duration = this.duration;
        copy.totallapsed = this.totallapsed;
        copy.lastOccupants = new HashSet<>();
        copy.newOccupants = new HashSet<>();
        copy.spatialQueryTimer = this.spatialQueryTimer;
        copy.zoneRef = this.zoneRef;
        copy.cachedPitch = this.cachedPitch;
        copy.cachedYaw = this.cachedYaw;
        copy.cachedRoll = this.cachedRoll;
        copy.cachedScale = this.cachedScale;
        copy.project();
        return copy;
    }
}
