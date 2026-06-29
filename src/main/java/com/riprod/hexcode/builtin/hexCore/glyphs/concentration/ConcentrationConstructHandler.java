package com.riprod.hexcode.builtin.hexCore.glyphs.concentration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.concentration.style.ConcentrationStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class ConcentrationConstructHandler implements ConstructHandler<ConcentrationState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float SECONDARY_INTERVAL = 1.0f;

    @Override
    public boolean onTick(float dt, HexStatus<ConcentrationState> status, ConstructTickContext ctx) {
        Ref<EntityStore> casterRef = ctx.getEntityRef();
        if (casterRef == null || !casterRef.isValid())
            return true;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        HexcasterIdleComponent execComp = buffer.getComponent(
                casterRef, HexcasterIdleComponent.getComponentType());
        if (execComp == null)
            return true;

        if (!execComp.isHoldingPrimary()) {
            fireReleaseAndKillHeld(status, buffer, casterRef);
            return true;
        }

        emitSecondary(dt, status, buffer, casterRef);

        return !drainSustain(dt, status);
    }

    private void fireReleaseAndKillHeld(HexStatus<ConcentrationState> status,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        Glyph trigger = status.getTriggeringGlyph();
        HexContext heldCtx = status.getHexContext();
        if (trigger == null || heldCtx == null)
            return;

        HexStats heldTracker = heldCtx.getHexStats();
        if (heldTracker != null && heldTracker.getCurrentVolatility() <= 0f)
            return;

        ConcentrationState state = status.getState();
        float bonus = state != null ? state.getVolatilityBonus() : 0f;
        if (heldTracker != null && bonus > 0f) {
            float adjusted = Math.max(0f, heldTracker.getCurrentVolatility() - bonus);
            heldTracker.setVolatility(adjusted);
        }

        HexContext releaseCtx = HexContext.cloneState(heldCtx);
        releaseCtx.updateRuntimeAccessors(buffer);

        HexcasterIdleComponent idle = buffer.getComponent(
                casterRef, HexcasterIdleComponent.getComponentType());
        if (idle != null) {
            idle.registerActiveTracker(releaseCtx.getHexStats());
        }

        if (heldTracker != null)
            heldTracker.setVolatility(0f);

        try {
            HexExecuter.continueFromSlot(trigger, ConcentrationGlyphSlots.RELEASE, releaseCtx);
        } catch (Exception e) {
            LOGGER.atWarning().log("concentration: cleanup fire failed: %s", e.getMessage());
        }
        ConcentrationStyle.renderEnd(
                buffer.getComponent(casterRef, TransformComponent.getComponentType()).getPosition(),
                releaseCtx, buffer);
    }

    private void emitSecondary(float dt, HexStatus<ConcentrationState> status,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        ConcentrationState state = status.getState();
        if (state == null)
            return;

        float accum = state.getTickAccum() + dt;
        while (accum >= SECONDARY_INTERVAL) {
            accum -= SECONDARY_INTERVAL;
            TransformComponent transform = buffer.getComponent(
                    casterRef, TransformComponent.getComponentType());
            if (transform == null)
                break;
            ConcentrationStyle.renderTick(transform.getPosition(), status.getHexContext(), buffer);
        }
        state.setTickAccum(accum);
    }

    @Override
    public void onCleanup(HexStatus<ConcentrationState> status, ConstructTickContext ctx) {
        ConcentrationState state = status.getState();
        if (state != null) {
            Ref<EntityStore> visualRef = state.getVisualRef();
            if (visualRef != null && visualRef.isValid()) {
                ctx.getBuffer().tryRemoveEntity(visualRef, RemoveReason.REMOVE);
            }
        }

        HexExecuter.fail(status.getHexContext());
    }
}
