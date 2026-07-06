package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DomainZoneComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, DomainZoneComponent> componentType;

    public static ComponentType<EntityStore, DomainZoneComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(ComponentType<EntityStore, DomainZoneComponent> type) {
        componentType = type;
    }

    private float radius;
    private Set<UUID> lastOccupants;
    private Set<UUID> newOccupants;
    private float durationSeconds;
    private float baseDrainPerSecond;
    private float baseTriggerCost;
    private float triggerDrainCost;
    private int triggerCount;
    private float power;
    private boolean contested;
    private UUID casterUuid;
    private Ref<EntityStore> casterRef;
    private Ref<EntityStore> zoneRef;
    private float ambientTimer;
    private float spatialQueryTimer;

    public DomainZoneComponent() {
    }

    public DomainZoneComponent(float radius, float durationSeconds, float baseDrainPerSecond, float baseTriggerCost,
            float power, UUID casterUuid, Ref<EntityStore> casterRef) {
        this.radius = radius;
        this.durationSeconds = durationSeconds;
        this.lastOccupants = new HashSet<>();
        this.newOccupants = new HashSet<>();
        this.baseDrainPerSecond = baseDrainPerSecond;
        this.baseTriggerCost = baseTriggerCost;
        this.triggerDrainCost = baseTriggerCost;
        this.triggerCount = 0;
        this.power = power;
        this.contested = false;
        this.casterUuid = casterUuid;
        this.casterRef = casterRef;
        this.ambientTimer = 0;
        this.spatialQueryTimer = 0;
    }

    public float getRadius() {
        return radius;
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

    public float getBaseDrainPerSecond() {
        return baseDrainPerSecond;
    }

    public float getBaseTriggerCost() {
        return baseTriggerCost;
    }

    public float getTriggerDrainCost() {
        return triggerDrainCost;
    }

    public void setTriggerDrainCost(float triggerDrainCost) {
        this.triggerDrainCost = triggerDrainCost;
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void incrementTriggerCount() {
        this.triggerCount++;
        this.triggerDrainCost = baseTriggerCost * (1 + triggerCount * 0.5f);
    }

    public float getPower() {
        return power;
    }

    public boolean isContested() {
        return contested;
    }

    public void setContested(boolean contested) {
        this.contested = contested;
    }

    public UUID getCasterUuid() {
        return casterUuid;
    }

    @Nullable
    public Ref<EntityStore> getCasterRef(ComponentAccessor<EntityStore> accessor) {
        return casterRef;
    }

    @Nullable
    public Ref<EntityStore> getZoneRef() {
        return zoneRef;
    }

    public void setZoneRef(Ref<EntityStore> zoneRef) {
        this.zoneRef = zoneRef;
    }

    public float getAmbientTimer() {
        return ambientTimer;
    }

    public void setAmbientTimer(float ambientTimer) {
        this.ambientTimer = ambientTimer;
    }

    public float getSpatialQueryTimer() {
        return spatialQueryTimer;
    }

    public void setSpatialQueryTimer(float spatialQueryTimer) {
        this.spatialQueryTimer = spatialQueryTimer;
    }

    public float getDurationSeconds() {
        return durationSeconds;
    }

    public boolean decrementSeconds(float dt) {
        this.durationSeconds -= dt;
        return this.durationSeconds <= 0;
    }

    @Nonnull
    @Override
    public DomainZoneComponent clone() {
        DomainZoneComponent copy = new DomainZoneComponent();
        copy.radius = this.radius;
        copy.lastOccupants = new HashSet<>();
        copy.newOccupants = new HashSet<>();
        copy.baseDrainPerSecond = this.baseDrainPerSecond;
        copy.baseTriggerCost = this.baseTriggerCost;
        copy.triggerDrainCost = this.triggerDrainCost;
        copy.triggerCount = this.triggerCount;
        copy.power = this.power;
        copy.contested = this.contested;
        copy.casterUuid = this.casterUuid;
        copy.casterRef = this.casterRef;
        copy.zoneRef = this.zoneRef;
        copy.durationSeconds = this.durationSeconds;
        copy.ambientTimer = this.ambientTimer;
        copy.spatialQueryTimer = this.spatialQueryTimer;
        return copy;
    }
}
