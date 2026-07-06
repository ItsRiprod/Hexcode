package com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class ScaleState implements ConstructState {

    private UUID constructId;
    private float appliedMagnitude = 1.0f;
    @Nullable
    private Ref<EntityStore> visualRef;
    private float remainingSeconds;
    @Nullable
    private String modelAssetId;
    private List<String> nextGlyphIds;

    public ScaleState() {
        this.constructId = UUID.randomUUID();
        this.nextGlyphIds = new ArrayList<>();
    }

    public ScaleState(UUID constructId, float appliedMagnitude, @Nullable Ref<EntityStore> visualRef,
            float remainingSeconds, @Nullable String modelAssetId, List<String> nextGlyphIds) {
        this.constructId = constructId;
        this.appliedMagnitude = appliedMagnitude;
        this.visualRef = visualRef;
        this.remainingSeconds = remainingSeconds;
        this.modelAssetId = modelAssetId;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public UUID getConstructId() {
        return constructId;
    }

    public float getAppliedMagnitude() {
        return appliedMagnitude;
    }

    @Nullable
    public Ref<EntityStore> getVisualRef() {
        return visualRef;
    }

    public void setVisualRef(@Nullable Ref<EntityStore> visualRef) {
        this.visualRef = visualRef;
    }

    public void setAppliedMagnitude(float magnitude) {
        this.appliedMagnitude = magnitude;
    }

    public void setModelAssetId(@Nullable String assetId) {
        this.modelAssetId = assetId;
    }

    public void setRemainingSeconds(float seconds) {
        this.remainingSeconds = seconds;
    }

    @Nullable
    public String getModelAssetId() {
        return modelAssetId;
    }

    public void tick(float dt) {
        remainingSeconds -= dt;
    }

    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void setNextGlyphIds(List<String> ids) {
        this.nextGlyphIds = ids != null ? ids : new ArrayList<>();
    }

    @Override
    public ScaleState copy() {
        return new ScaleState(constructId, appliedMagnitude, visualRef, remainingSeconds, modelAssetId,
                new ArrayList<>(nextGlyphIds));
    }
}
