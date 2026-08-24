package com.riprod.hexcode.core.common.construct.component;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class HexStatus<S extends ConstructState> {

    @Nullable
    private String handlerId;
    private boolean killRequested;

    private boolean firedFirstTick;

    private float elapsedTime = 0f;
    private UUID constructId;

    @Nonnull
    private HexContext hexContext;
    @Nullable
    private Glyph triggeringGlyph;

    @Nullable
    private S state;

    public HexStatus(@Nullable String handlerId,
            @Nonnull HexContext hexContext,
            @Nonnull UUID constructId,
            @Nullable Glyph triggeringGlyph) {
        this.handlerId = handlerId;
        this.killRequested = false;
        this.firedFirstTick = false;
        this.hexContext = hexContext;
        this.constructId = constructId;
        this.triggeringGlyph = triggeringGlyph;
    }

    public HexStatus(@Nullable String handlerId,
            @Nonnull HexContext hexContext,
            @Nonnull UUID constructId,
            @Nullable Glyph triggeringGlyph,
            @Nullable S state) {
        this(handlerId, hexContext, constructId, triggeringGlyph);
        this.state = state;
    }

    @Nullable
    public String getHandlerId() {
        return handlerId;
    }

    public void requestKill() {
        this.killRequested = true;
    }

    public boolean isKillRequested() {
        return killRequested;
    }

    public boolean hasFiredImmediate() {
        return firedFirstTick;
    }

    public void markImmediateFired() {
        this.firedFirstTick = true;
    }

    @Nonnull
    public HexContext getHexContext() {
        return hexContext;
    }

    @Nullable
    public Glyph getTriggeringGlyph() {
        return triggeringGlyph;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public float incrementElapsedTime(float dt) {
        this.elapsedTime += dt;
        return this.elapsedTime;
    }

    public void decrementElapsedTime(float dt) {
        this.elapsedTime -= dt;
    }

    public void resetElapsedTime() {
        this.elapsedTime = 0f;
    }

    public UUID getConstructId() {
        return constructId;
    }

    @Nullable
    public S getState() {
        return state;
    }

    public void setState(@Nullable S state) {
        this.state = state;
    }

}
