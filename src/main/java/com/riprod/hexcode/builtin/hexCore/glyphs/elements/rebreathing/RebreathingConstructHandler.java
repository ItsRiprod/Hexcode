package com.riprod.hexcode.builtin.hexCore.glyphs.elements.rebreathing;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;

public class RebreathingConstructHandler implements ConstructHandler<RebreathingState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        RebreathingState state = status.getState();
        if (state == null) return true;
        state.tick(dt);
        if (!drainSustain(dt, status)) return true;
        return state.isExpired();
    }

    @Override
    public void onEnd(HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        RebreathingState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("rebreathing: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("rebreathing: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<RebreathingState> status) {
        RebreathingState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<RebreathingState> status, List<String> ids) {
        RebreathingState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        RebreathingState state = status.getState();
        if (state == null) return;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> targetRef = ctx.getEntityRef();

        String effectId = state.getEffectId();
        if (effectId != null && targetRef != null && targetRef.isValid()) {
            EffectControllerComponent controller = buffer.getComponent(
                    targetRef, EffectControllerComponent.getComponentType());
            if (controller != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
                if (effectIndex != Integer.MIN_VALUE) {
                    controller.removeEffect(targetRef, effectIndex, buffer);
                }
            }
        }
    }
}
