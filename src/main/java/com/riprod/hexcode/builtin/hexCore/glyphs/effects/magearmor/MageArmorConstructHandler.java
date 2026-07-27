package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor;

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
import com.riprod.hexcode.core.common.execution.cast.VolatilityComponent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component.MagicHealthComponent;

public class MageArmorConstructHandler implements ConstructHandler<MageArmorState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        MageArmorState state = status.getState();
        if (state == null) return true;
        state.tick(dt);
        if (!drainSustain(dt, status)) return true;
        if (isPoolDepleted(status)) return true;
        return state.isExpired();
    }

    private boolean isPoolDepleted(HexStatus<MageArmorState> status) {
        VolatilityComponent stats = status.getHexContext().volatility();
        return stats == null || stats.getCurrent() <= 0f;
    }

    @Override
    public void onEnd(HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        MageArmorState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("magearmor: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("magearmor: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<MageArmorState> status) {
        MageArmorState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<MageArmorState> status, List<String> ids) {
        MageArmorState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        MageArmorState state = status.getState();
        if (state == null || state.isCleanedUp()) return;
        state.markCleanedUp();

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> targetRef = ctx.getEntityRef();
        if (targetRef == null || !targetRef.isValid()) return;

        String effectId = state.getEffectId();
        if (effectId != null) {
            EffectControllerComponent controller = buffer.getComponent(
                    targetRef, EffectControllerComponent.getComponentType());
            if (controller != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
                if (effectIndex != Integer.MIN_VALUE) {
                    controller.removeEffect(targetRef, effectIndex, buffer);
                }
            }
        }

        buffer.tryRemoveComponent(targetRef, MagicHealthComponent.getComponentType());
    }
}
