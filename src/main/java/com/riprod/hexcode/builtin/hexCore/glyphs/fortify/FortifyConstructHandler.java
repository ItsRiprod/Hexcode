package com.riprod.hexcode.builtin.hexCore.glyphs.fortify;

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

public class FortifyConstructHandler implements ConstructHandler<FortifyState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<FortifyState> status, ConstructTickContext ctx) {
        FortifyState state = status.getState();
        if (state == null) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<FortifyState> status, ConstructTickContext ctx) {
        FortifyState state = status.getState();
        cleanup(ctx, state != null ? state.getEffectId() : null);
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("fortify: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<FortifyState> status, ConstructTickContext ctx) {
        FortifyState state = status.getState();
        cleanup(ctx, state != null ? state.getEffectId() : null);
        LOGGER.atInfo().log("fortify: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<FortifyState> status) {
        FortifyState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<FortifyState> status, List<String> ids) {
        FortifyState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(ConstructTickContext ctx, String effectId) {
        if (effectId == null) return;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid()) return;

        EffectControllerComponent controller = buffer.getComponent(
                target, EffectControllerComponent.getComponentType());
        if (controller != null) {
            int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
            if (effectIndex != Integer.MIN_VALUE) {
                controller.removeEffect(target, effectIndex, buffer);
            }
        }
    }
}
