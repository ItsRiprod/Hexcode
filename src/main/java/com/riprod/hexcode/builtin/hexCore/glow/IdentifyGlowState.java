package com.riprod.hexcode.builtin.hexCore.glow;

import javax.annotation.Nullable;

import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class IdentifyGlowState implements ConstructState {

    private final PersistentRef viewer;
    @Nullable
    private final PersistentRef target;
    @Nullable
    private final Vector3i blockPos;
    private final String volumeId;
    private final Vector3f color;
    private float remainingSeconds;

    public IdentifyGlowState(PersistentRef viewer, @Nullable PersistentRef target,
            @Nullable Vector3i blockPos, String volumeId, Vector3f color, float remainingSeconds) {
        this.viewer = viewer;
        this.target = target;
        this.blockPos = blockPos;
        this.volumeId = volumeId;
        this.color = color;
        this.remainingSeconds = remainingSeconds;
    }

    public PersistentRef getViewer() {
        return viewer;
    }

    @Nullable
    public PersistentRef getTarget() {
        return target;
    }

    @Nullable
    public Vector3i getBlockPos() {
        return blockPos;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public Vector3f getColor() {
        return color;
    }

    public boolean isEntity() {
        return target != null;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public void tick(float dt) {
        remainingSeconds -= dt;
    }

    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }

    @Override
    public IdentifyGlowState copy() {
        return new IdentifyGlowState(viewer, target,
                blockPos == null ? null : new Vector3i(blockPos),
                volumeId, new Vector3f(color), remainingSeconds);
    }
}
