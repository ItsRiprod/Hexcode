package com.riprod.hexcode.api.event;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;

/** All Crafting-Related ECS actions and events */
public class CraftingEvent implements IEvent<Void> {

    public enum Reason {
        ENTERED_SELECTING,
        ENTERED_CRAFTING,
        EXITED_NORMAL,
        EXITED_PEDESTAL_BROKEN,

        DENIED_OUT_OF_RANGE,
        DENIED_PEDESTAL_BUSY,

        ERROR_INVALID_HEX;

        public boolean isExit() {
            return this == EXITED_NORMAL || this == EXITED_PEDESTAL_BROKEN;
        }

        public boolean isDenial() {
            return this == DENIED_OUT_OF_RANGE || this == DENIED_PEDESTAL_BUSY;
        }

        public boolean isError() {
            return this == ERROR_INVALID_HEX;
        }
    }

    private final Reason reason;
    private final Ref<EntityStore> playerRef;
    @Nullable
    private final Vector3i pedestalLocation;
    @Nullable
    private final PedestalBlockComponent pedestal;
    @Nullable
    private final Hex hex;
    @Nullable
    private final String slotKey;
    @Nullable
    private final String message;

    private boolean cancelled;

    private CraftingEvent(Builder b) {
        this.reason = b.reason;
        this.playerRef = b.playerRef;
        this.pedestalLocation = b.pedestalLocation;
        this.pedestal = b.pedestal;
        this.hex = b.hex;
        this.slotKey = b.slotKey;
        this.message = b.message;
    }

    public Reason getReason() {
        return reason;
    }

    public Ref<EntityStore> getPlayerRef() {
        return playerRef;
    }

    @Nullable
    public Vector3i getPedestalLocation() {
        return pedestalLocation;
    }

    @Nullable
    public PedestalBlockComponent getPedestal() {
        return pedestal;
    }

    @Nullable
    public Hex getHex() {
        return hex;
    }

    @Nullable
    public String getSlotKey() {
        return slotKey;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static Builder builder(Reason reason, Ref<EntityStore> playerRef) {
        return new Builder(reason, playerRef);
    }

    public static class Builder {
        private final Reason reason;
        private final Ref<EntityStore> playerRef;
        @Nullable private Vector3i pedestalLocation;
        @Nullable private PedestalBlockComponent pedestal;
        @Nullable private Hex hex;
        @Nullable private String slotKey;
        @Nullable private String message;

        private Builder(Reason reason, Ref<EntityStore> playerRef) {
            this.reason = reason;
            this.playerRef = playerRef;
        }

        public Builder pedestalLocation(@Nullable Vector3i v) { this.pedestalLocation = v; return this; }
        public Builder pedestal(@Nullable PedestalBlockComponent p) {
            this.pedestal = p;
            if (p != null && p.getLocation() != null && this.pedestalLocation == null) {
                this.pedestalLocation = p.getLocation();
            }
            return this;
        }
        public Builder hex(@Nullable Hex h) { this.hex = h; return this; }
        public Builder slotKey(@Nullable String s) { this.slotKey = s; return this; }
        public Builder message(@Nullable String m) { this.message = m; return this; }

        public CraftingEvent build() { return new CraftingEvent(this); }
    }
}
