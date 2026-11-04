package com.riprod.hexcode.builtin.elementalism.status.block;

import javax.annotation.Nullable;

import com.riprod.hexcode.builtin.elementalism.effects.EffectEntry;
import com.riprod.hexcode.builtin.elementalism.effects.EffectSlot;
import com.riprod.hexcode.builtin.elementalism.effects.ElementEffects;
import com.riprod.hexcode.builtin.elementalism.status.StatusAsset;

public final class BlockStatusEntry {
    private final String statusId;
    private float remaining;
    private float auraCooldown;
    private final int blockId;

    public BlockStatusEntry(String statusId, float duration, int blockId) {
        this(statusId, duration, 0f, blockId);
    }

    private BlockStatusEntry(String statusId, float remaining, float auraCooldown, int blockId) {
        this.statusId = statusId;
        this.remaining = remaining;
        this.auraCooldown = auraCooldown;
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

    @Nullable
    public EffectEntry dueAura(float dt) {
        this.auraCooldown -= dt;
        if (this.auraCooldown > 0f) {
            return null;
        }
        var asset = StatusAsset.getAssetMap().getAsset(this.statusId);
        if (asset == null) {
            return null;
        }
        this.auraCooldown = asset.getAuraInterval();
        return ElementEffects.resolve(null, asset.getEffects()).get(EffectSlot.AURA);
    }

    BlockStatusEntry copy() {
        return new BlockStatusEntry(this.statusId, this.remaining, this.auraCooldown, this.blockId);
    }
}
