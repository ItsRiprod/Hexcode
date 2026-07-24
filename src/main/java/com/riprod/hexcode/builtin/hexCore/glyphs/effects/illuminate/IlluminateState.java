package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class IlluminateState implements ConstructState {

    private final boolean showBox;
    private final boolean spawnedOwner;
    private final String volumeId;
    private final Vector3f boxColor;
    private final List<String> nextGlyphIds;
    private float remainingSeconds;

    public IlluminateState(float remainingSeconds, boolean showBox, boolean spawnedOwner,
            String volumeId, Vector3f boxColor, List<String> nextGlyphIds) {
        this.remainingSeconds = remainingSeconds;
        this.showBox = showBox;
        this.spawnedOwner = spawnedOwner;
        this.volumeId = volumeId;
        this.boxColor = boxColor;
        this.nextGlyphIds = nextGlyphIds != null ? nextGlyphIds : new ArrayList<>();
    }

    public boolean isShowBox() {
        return showBox;
    }

    public boolean isSpawnedOwner() {
        return spawnedOwner;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public Vector3f getBoxColor() {
        return boxColor;
    }

    public List<String> getNextGlyphIds() {
        return nextGlyphIds;
    }

    public void tick(float dt) {
        remainingSeconds -= dt;
    }

    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }

    @Override
    public IlluminateState copy() {
        return new IlluminateState(remainingSeconds, showBox, spawnedOwner, volumeId,
                new Vector3f(boxColor), new ArrayList<>(nextGlyphIds));
    }
}
