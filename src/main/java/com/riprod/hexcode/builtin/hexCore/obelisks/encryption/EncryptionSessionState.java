package com.riprod.hexcode.builtin.hexCore.obelisks.encryption;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.obelisk.ObeliskSessionState;

public class EncryptionSessionState implements ObeliskSessionState {

    private boolean unlocked;
    private boolean captureArmed;
    private Ref<EntityStore> displayRef;

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isCaptureArmed() {
        return captureArmed;
    }

    public void setCaptureArmed(boolean captureArmed) {
        this.captureArmed = captureArmed;
    }

    @Nullable
    public Ref<EntityStore> getDisplayRef() {
        return displayRef;
    }

    public void setDisplayRef(@Nullable Ref<EntityStore> displayRef) {
        this.displayRef = displayRef;
    }

    @Override
    public void onTeardown(CommandBuffer<EntityStore> buffer) {
        EncodingDisplay.despawn(buffer, this);
    }
}
