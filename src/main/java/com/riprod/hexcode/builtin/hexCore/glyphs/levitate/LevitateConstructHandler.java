package com.riprod.hexcode.builtin.hexCore.glyphs.levitate;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.levitate.style.LevitateStyle;

public class LevitateConstructHandler implements ConstructHandler<LevitateState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String LEVITATE_EFFECT_ID = "Hexcode_Levitate";
    private static final float TICK_INTERVAL = 0.5f;

    @Override
    public boolean onTick(float dt, HexStatus<LevitateState> status, ConstructTickContext ctx) {
        LevitateState state = status.getState();
        if (state == null)
            return true;
        if (state.isExpired())
            return true;
        state.tick(dt);

        emitTickVfx(dt, state, status, ctx);

        return !drainSustain(dt, status);
    }

    private void emitTickVfx(float dt, LevitateState state, HexStatus<LevitateState> status,
            ConstructTickContext ctx) {
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid())
            return;

        float accum = state.getTickAccum() + dt;
        while (accum >= TICK_INTERVAL) {
            accum -= TICK_INTERVAL;
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
            LevitateState state = status.getState();
            if (state == null) return;

            CommandBuffer<EntityStore> buffer = ctx.getBuffer();
            Ref<EntityStore> target = ctx.getEntityRef();
            if (target == null || !target.isValid()) return;

            LevitateStackComponent stack = buffer.getComponent(
                    target, LevitateStackComponent.getComponentType());
            if (stack != null) {
                stack.remove(state.getConstructId());
                if (stack.isEmpty()) {
                    LevitateGlyph.clearLevitation(target, buffer);
                    buffer.removeComponent(target, LevitateStackComponent.getComponentType());
                } else {
                    buffer.putComponent(target, LevitateStackComponent.getComponentType(), stack);
                }
            }

            EffectControllerComponent controller = buffer.getComponent(
                    target, EffectControllerComponent.getComponentType());
            if (controller != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex(LEVITATE_EFFECT_ID);
                if (effectIndex != Integer.MIN_VALUE) {
                    controller.removeEffect(target, effectIndex, buffer);
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("levitate cleanup failed: %s", e.getMessage());
        }
    }
}
