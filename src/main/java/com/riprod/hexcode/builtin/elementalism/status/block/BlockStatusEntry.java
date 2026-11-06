package com.riprod.hexcode.builtin.elementalism.status.block;

import com.riprod.hexcode.builtin.elementalism.status.StatusAsset;
import com.riprod.hexcode.core.common.effects.component.SustainState;

public final class BlockStatusEntry {
    private final String statusId;
    private float remaining;
    private final SustainState sustain = new SustainState();
    private final int blockId;

    public BlockStatusEntry(String statusId, float remaining, int blockId) {
        this.statusId = statusId;
        this.remaining = remaining;
        this.blockId = blockId;
    }

    public String getStatusId() {
        return this.statusId;
    }

    public int getBlockId() {
        return this.blockId;
    }

    public void extend(float duration) {
        this.remaining += duration;
    }

    public boolean advance(float dt) {
        this.remaining -= dt;
        return this.remaining > 0f && StatusAsset.getAssetMap().getAsset(this.statusId) != null;
    }

    public SustainState getSustain() {
        return this.sustain;
    }

    BlockStatusEntry copy() {
        return new BlockStatusEntry(this.statusId, this.remaining, this.blockId);
    }
}
