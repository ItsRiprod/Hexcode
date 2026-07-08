package com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze.component;

import org.joml.Vector3i;

public class FrozenBlock {

    private final Vector3i position;
    private final String blockTypeId;
    private final int rotationIndex;

    public FrozenBlock(Vector3i position, String blockTypeId, int rotationIndex) {
        this.position = position;
        this.blockTypeId = blockTypeId;
        this.rotationIndex = rotationIndex;
    }

    public Vector3i getPosition() {
        return position;
    }

    public String getBlockTypeId() {
        return blockTypeId;
    }

    public int getRotationIndex() {
        return rotationIndex;
    }
}
