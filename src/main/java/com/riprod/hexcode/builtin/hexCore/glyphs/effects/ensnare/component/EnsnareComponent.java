package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EnsnareComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, EnsnareComponent> componentType;

    private List<SpikeEntry> spikes;
    private float durationSeconds;
    private float elapsedSeconds;
    private float spikeDamage;
    private float damageCooldownSeconds;
    private Map<UUID, Float> lastDamageTimeMap;
    private transient Map<Long, List<SpikeEntry>> spikesByCell;
    private Vector3d center;
    private double radius;
    private double spikeHitYMin;
    private double spikeHitYMax;

    public EnsnareComponent() {
    }

    public EnsnareComponent(List<SpikeEntry> spikes, float durationSeconds, float spikeDamage,
            float damageCooldownSeconds, Vector3d center, double radius,
            double spikeHitYMin, double spikeHitYMax) {
        this.spikes = spikes;
        this.durationSeconds = durationSeconds;
        this.elapsedSeconds = 0;
        this.spikeDamage = spikeDamage;
        this.damageCooldownSeconds = damageCooldownSeconds;
        this.lastDamageTimeMap = new HashMap<>();
        this.center = center;
        this.radius = radius;
        this.spikeHitYMin = spikeHitYMin;
        this.spikeHitYMax = spikeHitYMax;
    }

    public static void setComponentType(ComponentType<EntityStore, EnsnareComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, EnsnareComponent> getComponentType() {
        return componentType;
    }

    public List<SpikeEntry> getSpikes() {
        return spikes;
    }

    public SpikeEntry findNearestSpike(Vector3d entityPos, double hitRadiusSq) {
        if (entityPos == null || spikes == null || spikes.isEmpty()) return null;

        ensureSpikeIndex();

        SpikeEntry nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        int cellX = (int) Math.floor(entityPos.x);
        int cellZ = (int) Math.floor(entityPos.z);

        for (int x = cellX - 1; x <= cellX + 1; x++) {
            for (int z = cellZ - 1; z <= cellZ + 1; z++) {
                List<SpikeEntry> bucket = spikesByCell.get(cellKey(x, z));
                if (bucket == null) continue;

                for (SpikeEntry spike : bucket) {
                    Vector3d spikePos = spike.getPosition();
                    double dx = entityPos.x - spikePos.x;
                    double dz = entityPos.z - spikePos.z;
                    double distSq = dx * dx + dz * dz;

                    if (distSq < nearestDistSq && distSq <= hitRadiusSq) {
                        double dy = entityPos.y - spikePos.y;
                        if (dy >= spikeHitYMin && dy <= spikeHitYMax) {
                            nearestDistSq = distSq;
                            nearest = spike;
                        }
                    }
                }
            }
        }

        return nearest;
    }

    public float getDurationSeconds() {
        return durationSeconds;
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void incrementElapsed(float dt) {
        elapsedSeconds += dt;
    }

    public boolean isExpired() {
        return elapsedSeconds >= durationSeconds;
    }

    public float getSpikeDamage() {
        return spikeDamage;
    }

    public Vector3d getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public boolean canDamageTarget(UUID targetId) {
        ensureDamageMap();
        Float lastTime = lastDamageTimeMap.get(targetId);
        if (lastTime == null) return true;
        return (elapsedSeconds - lastTime) >= damageCooldownSeconds;
    }

    public void recordDamage(UUID targetId) {
        ensureDamageMap();
        lastDamageTimeMap.put(targetId, elapsedSeconds);
    }

    private void ensureDamageMap() {
        if (lastDamageTimeMap == null) {
            lastDamageTimeMap = new HashMap<>();
        }
    }

    private void ensureSpikeIndex() {
        if (spikesByCell != null) return;

        spikesByCell = new HashMap<>();
        for (SpikeEntry spike : spikes) {
            if (spike == null || spike.getPosition() == null) continue;
            Vector3d spikePos = spike.getPosition();
            int cellX = (int) Math.floor(spikePos.x);
            int cellZ = (int) Math.floor(spikePos.z);
            spikesByCell.computeIfAbsent(cellKey(cellX, cellZ), key -> new ArrayList<>()).add(spike);
        }
    }

    private static long cellKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    @Nonnull
    @Override
    public EnsnareComponent clone() {
        EnsnareComponent copy = new EnsnareComponent();
        copy.spikes = this.spikes != null ? new ArrayList<>(this.spikes) : null;
        copy.durationSeconds = this.durationSeconds;
        copy.elapsedSeconds = this.elapsedSeconds;
        copy.spikeDamage = this.spikeDamage;
        copy.damageCooldownSeconds = this.damageCooldownSeconds;
        copy.lastDamageTimeMap = this.lastDamageTimeMap != null ? new HashMap<>(this.lastDamageTimeMap) : null;
        copy.center = this.center != null ? new Vector3d(this.center) : null;
        copy.radius = this.radius;
        copy.spikeHitYMin = this.spikeHitYMin;
        copy.spikeHitYMax = this.spikeHitYMax;
        return copy;
    }
}
