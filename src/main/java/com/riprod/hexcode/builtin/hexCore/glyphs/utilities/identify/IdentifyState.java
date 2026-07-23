package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.identify;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class IdentifyState implements ConstructState {

    public static final class Glow {
        private final PersistentRef viewer;
        @Nullable
        private final PersistentRef target;
        @Nullable
        private final Vector3i blockPos;
        private final String volumeId;
        private final Vector3f color;

        public Glow(PersistentRef viewer, @Nullable PersistentRef target,
                @Nullable Vector3i blockPos, String volumeId, Vector3f color) {
            this.viewer = viewer;
            this.target = target;
            this.blockPos = blockPos;
            this.volumeId = volumeId;
            this.color = color;
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

        Glow copy() {
            return new Glow(viewer, target, blockPos == null ? null : new Vector3i(blockPos),
                    volumeId, new Vector3f(color));
        }
    }

    private final List<Glow> glows;
    private final String effectId;
    private float remainingSeconds;

    public IdentifyState(String effectId) {
        this.glows = new ArrayList<>();
        this.effectId = effectId;
    }

    private IdentifyState(List<Glow> glows, String effectId, float remainingSeconds) {
        this.glows = glows;
        this.effectId = effectId;
        this.remainingSeconds = remainingSeconds;
    }

    public List<Glow> getGlows() {
        return glows;
    }

    public String getEffectId() {
        return effectId;
    }

    public void add(Glow glow) {
        glows.add(glow);
    }

    public void extend(float seconds) {
        if (seconds > remainingSeconds) {
            remainingSeconds = seconds;
        }
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
    public IdentifyState copy() {
        List<Glow> copied = new ArrayList<>(glows.size());
        for (Glow g : glows) {
            copied.add(g.copy());
        }
        return new IdentifyState(copied, effectId, remainingSeconds);
    }
}
