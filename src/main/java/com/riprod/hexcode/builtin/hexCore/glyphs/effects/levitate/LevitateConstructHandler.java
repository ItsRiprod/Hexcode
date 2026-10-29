package com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate.style.LevitateStyle;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VelocityUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class LevitateConstructHandler implements ConstructHandler<LevitateState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);
    private static final float TERMINAL_VELOCITY = 130.0f;

    @Override
    public boolean onTick(float dt, HexStatus<LevitateState> status, ConstructTickContext ctx) {
        LevitateState state = status.getState();
        if (state == null)
            return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId()))
            return true;
        if (state.isExpired())
            return true;
        state.tick(dt);

        var asset = GlyphAsset.of(status.getTriggeringGlyph(), LevitateGlyph.class);
        if (asset != null) {
            applyRise(dt, state, asset, ctx);
            emitTickVfx(dt, state, asset, status, ctx);
        }

        drainSustain(dt, status);
        return false;
    }

    private void applyRise(float dt, LevitateState state, LevitateGlyph asset, ConstructTickContext ctx) {
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid())
            return;
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        double targetVy = Math.min(asset.getRiseSpeedPerIntensity() * state.getAppliedIntensity(),
                TERMINAL_VELOCITY);
        if (targetVy <= 0)
            return;

        if (buffer.getComponent(target, Player.getComponentType()) != null) {
            Velocity vel = buffer.getComponent(target, Velocity.getComponentType());
            if (vel == null)
                return;
            double currentVy = VelocityUtil.currentVelocity(target, buffer).y;
            if (currentVy >= targetVy)
                return;
            double delta = Math.min(targetVy - currentVy, asset.getMaxCatchAccel() * dt);
            if (delta <= 0)
                return;
            vel.addInstruction(new Vector3d(0, delta, 0), null, ChangeVelocityType.Add);
        } else {
            VelocityUtil.applyVelocity(target, new Vector3d(0, targetVy, 0),
                    ChangeVelocityType.Set, null, buffer);
        }
    }

    private void emitTickVfx(float dt, LevitateState state, LevitateGlyph asset,
            HexStatus<LevitateState> status, ConstructTickContext ctx) {
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid())
            return;

        float tickInterval = asset.getTickInterval();
        float accum = state.getTickAccum() + dt;
        while (accum >= tickInterval) {
            accum -= tickInterval;
            TransformComponent transform = ctx.getBuffer().getComponent(
                    target, TransformComponent.getComponentType());
            if (transform == null)
                break;
            LevitateStyle.renderTick(asset, transform.getPosition(), status.getHexContext(), ctx.getBuffer());
        }
        state.setTickAccum(accum);
    }

    @Override
    public void onEnd(HexStatus<LevitateState> status, ConstructTickContext ctx) {
        removeEffect(status, ctx);
        LevitateState state = status.getState();
        if (state == null) {
            status.getHexContext().endBranch();
            return;
        }
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atFine().log("levitate: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<LevitateState> status, ConstructTickContext ctx) {
        removeEffect(status, ctx);
        LOGGER.atFine().log("levitate: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<LevitateState> status) {
        LevitateState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<LevitateState> status, List<String> ids) {
        LevitateState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void removeEffect(HexStatus<LevitateState> status, ConstructTickContext ctx) {
        var state = status.getState();
        VfxUtil.removeEffect(ctx.getBuffer(), ctx.getEntityRef(), state != null ? state.getEffectId() : null);
    }
}
