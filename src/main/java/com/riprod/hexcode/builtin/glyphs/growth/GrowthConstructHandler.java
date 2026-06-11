package com.riprod.hexcode.builtin.glyphs.growth;

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

public class GrowthConstructHandler implements ConstructHandler<GrowthState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String GROWTH_EFFECT_ID = "Hexcode_Growth";

    @Override
    public boolean onTick(float dt, HexStatus<GrowthState> status, ConstructTickContext ctx) {
        GrowthState state = status.getState();
        if (state == null) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<GrowthState> status, ConstructTickContext ctx) {
        cleanup(ctx);
        GrowthState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("growth: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<GrowthState> status, ConstructTickContext ctx) {
        cleanup(ctx);
        LOGGER.atInfo().log("growth: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<GrowthState> status) {
        GrowthState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<GrowthState> status, List<String> ids) {
        GrowthState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(ConstructTickContext ctx) {
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid()) return;

        EffectControllerComponent controller = buffer.getComponent(
                target, EffectControllerComponent.getComponentType());
        if (controller != null) {
            int effectIndex = EntityEffect.getAssetMap().getIndex(GROWTH_EFFECT_ID);
            if (effectIndex != Integer.MIN_VALUE) {
                controller.removeEffect(target, effectIndex, buffer);
            }
        }
    }
}
