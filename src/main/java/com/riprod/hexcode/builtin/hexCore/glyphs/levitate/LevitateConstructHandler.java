package com.riprod.hexcode.builtin.hexCore.glyphs.levitate;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.levitate.style.LevitateStyle;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class LevitateConstructHandler implements ConstructHandler<LevitateState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<LevitateState> status, ConstructTickContext ctx) {
        LevitateState state = status.getState();
        if (state == null)
            return true;
        if (state.isExpired())
            return true;
        state.tick(dt);

        LevitateConfig config = resolveConfig(status);

        applyRise(state, config, ctx);
        emitTickVfx(dt, state, config, status, ctx);

        return !drainSustain(dt, status);
    }

    private LevitateConfig resolveConfig(HexStatus<LevitateState> status) {
        Glyph glyph = status.getTriggeringGlyph();
        GlyphAsset asset = glyph != null ? GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId()) : null;
        return asset != null && asset.getConfig() instanceof LevitateConfig config
                ? config
                : LevitateConfig.DEFAULTS;
    }

    private void applyRise(LevitateState state, LevitateConfig config, ConstructTickContext ctx) {
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid())
            return;
        Velocity vel = ctx.getBuffer().getComponent(target, Velocity.getComponentType());
        if (vel == null)
            return;
        double rise = config.getRiseSpeedPerIntensity() * state.getAppliedIntensity();
        vel.addInstruction(new Vector3d(0, rise, 0), null, ChangeVelocityType.Set);
    }

    private void emitTickVfx(float dt, LevitateState state, LevitateConfig config,
            HexStatus<LevitateState> status, ConstructTickContext ctx) {
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid())
            return;

        float tickInterval = config.getTickInterval();
        float accum = state.getTickAccum() + dt;
        while (accum >= tickInterval) {
            accum -= tickInterval;
            TransformComponent transform = ctx.getBuffer().getComponent(
                    target, TransformComponent.getComponentType());
            if (transform == null)
                break;
            LevitateStyle.renderTick(transform.getPosition(), status.getHexContext(), ctx.getBuffer());
        }
        state.setTickAccum(accum);
    }

    @Override
    public void onEnd(HexStatus<LevitateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LevitateState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("levitate: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<LevitateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("levitate: terminated early; chain suppressed");
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

    private void cleanup(HexStatus<LevitateState> status, ConstructTickContext ctx) {
        try {
            CommandBuffer<EntityStore> buffer = ctx.getBuffer();
            Ref<EntityStore> target = ctx.getEntityRef();
            if (target == null || !target.isValid()) return;

            LevitateState state = status.getState();
            String effectId = state != null ? state.getEffectId() : null;
            if (effectId == null) return;

            EffectControllerComponent controller = buffer.getComponent(
                    target, EffectControllerComponent.getComponentType());
            if (controller != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
                if (effectIndex != Integer.MIN_VALUE) {
                    controller.removeEffect(target, effectIndex, buffer);
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("levitate cleanup failed: %s", e.getMessage());
        }
    }
}
