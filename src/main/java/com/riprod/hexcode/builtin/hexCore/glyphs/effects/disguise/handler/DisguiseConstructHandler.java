package com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.handler;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.components.DisguiseState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.style.DisguiseStyle;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;

public class DisguiseConstructHandler implements ConstructHandler<DisguiseState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<DisguiseState> status, ConstructTickContext ctx) {
        DisguiseState state = status.getState();
        if (state == null) return true;
        if (effectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<DisguiseState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        DisguiseState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("Disguise: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<DisguiseState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("Disguise: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<DisguiseState> status) {
        DisguiseState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<DisguiseState> status, List<String> ids) {
        DisguiseState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<DisguiseState> status, ConstructTickContext ctx) {
        try {
            DisguiseState state = status.getState();
            if (state == null) return;

            CommandBuffer<EntityStore> buffer = ctx.getBuffer();
            Ref<EntityStore> targetRef = ctx.getEntityRef();
            if (targetRef == null || !targetRef.isValid()) return;

            HexAppearanceService.removeLayer(buffer, targetRef, state.getConstructId().toString());
            removeEffect(buffer, targetRef, state.getEffectId());

            TransformComponent tc = buffer.getComponent(targetRef, TransformComponent.getComponentType());
            if (tc != null) {
                DisguiseStyle.renderRestore(tc.getPosition(), status.getHexContext(), buffer);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] DisguiseConstructHandler cleanup failed: %s", e.getMessage());
        }
    }

    private static boolean effectRemoved(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> target, String effectId) {
        if (effectId == null || target == null || !target.isValid()) return false;
        int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
        if (effectIndex == Integer.MIN_VALUE) return false;
        EffectControllerComponent controller = buffer.getComponent(
                target, EffectControllerComponent.getComponentType());
        if (controller == null) return false;
        return !controller.hasEffect(effectIndex);
    }

    private static void removeEffect(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> target, String effectId) {
        if (effectId == null) return;
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
