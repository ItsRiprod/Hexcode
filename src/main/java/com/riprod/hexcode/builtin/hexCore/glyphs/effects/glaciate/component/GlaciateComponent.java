package com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class GlaciateComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, GlaciateComponent> componentType;

    private float damageRadius;
    private float damageMultiplier;
    private float durationSeconds;
    private Set<UUID> hitEntities;
    private boolean firedFirstBranch;

    public GlaciateComponent() {
    }

    public GlaciateComponent(float damageRadius, float damageMultiplier, float durationSeconds) {
        this.damageRadius = damageRadius;
        this.damageMultiplier = damageMultiplier;
        this.hitEntities = new HashSet<>();
        this.durationSeconds = durationSeconds;
        this.firedFirstBranch = false;
    }

    public static void setComponentType(ComponentType<EntityStore, GlaciateComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, GlaciateComponent> getComponentType() {
        return componentType;
    }

    public float getDamageRadius() {
        return damageRadius;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public Set<UUID> getHitEntities() {
        if (hitEntities == null) hitEntities = new HashSet<>();
        return hitEntities;
    }

    public boolean firedFirstBranch() {
        return firedFirstBranch;
    }

    public void markFirstBranchFired() {
        this.firedFirstBranch = true;
    }


    public boolean tickDuration(float dt) {
        this.durationSeconds -= dt;
        return this.durationSeconds > 0;
    }

    @Nonnull
    @Override
    public GlaciateComponent clone() {
        GlaciateComponent copy = new GlaciateComponent();
        copy.damageRadius = this.damageRadius;
        copy.damageMultiplier = this.damageMultiplier;
        copy.durationSeconds = this.durationSeconds;
        copy.hitEntities = this.hitEntities != null ? new HashSet<>(this.hitEntities) : new HashSet<>();
        copy.firedFirstBranch = this.firedFirstBranch;
        return copy;
    }
}
