package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

public class PhasedBlock {

    private final Vector3i position;
    private final String blockTypeId;
    private final int rotationIndex;
    private final Holder<ChunkStore> blockEntity;

    public PhasedBlock(Vector3i position, String blockTypeId, int rotationIndex,
            Holder<ChunkStore> blockEntity) {
        this.position = position;
        this.blockTypeId = blockTypeId;
        this.rotationIndex = rotationIndex;
        this.blockEntity = blockEntity;
    }

    public Holder<ChunkStore> getBlockEntity() {
        return blockEntity;
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
