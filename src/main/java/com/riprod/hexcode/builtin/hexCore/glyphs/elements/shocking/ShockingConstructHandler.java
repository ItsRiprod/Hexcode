package com.riprod.hexcode.builtin.hexCore.glyphs.elements.shocking;

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

public class ShockingConstructHandler implements ConstructHandler<ShockingState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<ShockingState> status, ConstructTickContext ctx) {
        ShockingState state = status.getState();
        if (state == null) return true;
        state.tick(dt);
        if (!drainSustain(dt, status)) return true;
        return state.isExpired();
    }

    @Override
    public void onEnd(HexStatus<ShockingState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        ShockingState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("shocking: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<ShockingState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("shocking: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<ShockingState> status) {
        ShockingState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<ShockingState> status, List<String> ids) {
        ShockingState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<ShockingState> status, ConstructTickContext ctx) {
        ShockingState state = status.getState();
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
