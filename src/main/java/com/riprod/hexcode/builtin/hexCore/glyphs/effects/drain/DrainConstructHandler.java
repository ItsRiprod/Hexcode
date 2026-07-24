package com.riprod.hexcode.builtin.hexCore.glyphs.effects.drain;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.drain.style.DrainStyle;

public class DrainConstructHandler implements ConstructHandler<DrainState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<DrainState> status, ConstructTickContext ctx) {
        DrainState state = status.getState();
        if (state == null) return true;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> target = ctx.getEntityRef();

        if (effectRemoved(buffer, target, state.getEffectId())) return true;

        EntityStatMap statMap = buffer.getComponent(target, EntityStatMap.getComponentType());
        if (statMap == null) return true;
        if (state.isExpired()) return true;

        EntityStatValue sourceStat = statMap.get(state.getSourceStatIndex());
        if (sourceStat == null) return true;

        if (state.getSourceStatIndex() == DefaultEntityStatTypes.getHealth()) {
            if (sourceStat.get() <= state.getHpFloor()) return true;
        } else {
            if (sourceStat.get() <= 0) return true;
        }

        Ref<EntityStore> destRef = state.getDestEntityRef();
        int manaIndex = DefaultEntityStatTypes.getMana();
        EntityStatMap destStatMap = null;
        if (destRef != null && destRef.isValid()) {
            destStatMap = buffer.getComponent(destRef, EntityStatMap.getComponentType());
            if (destStatMap != null) {
                EntityStatValue destMana = destStatMap.get(manaIndex);
                if (destMana != null && destMana.get() >= destMana.getMax()) return true;
            }
        }

        float drainAmount = state.getDrainPerSecond() * dt;
        if (state.getSourceStatIndex() == DefaultEntityStatTypes.getHealth()) {
            drainAmount = Math.min(drainAmount, sourceStat.get() - state.getHpFloor());
        } else {
            drainAmount = Math.min(drainAmount, sourceStat.get());
        }
        if (drainAmount <= 0) return true;

        statMap.subtractStatValue(state.getSourceStatIndex(), drainAmount);

        float converted = drainAmount * state.getConversionRate();
        if (destStatMap != null) {
            EntityStatValue destMana = destStatMap.get(manaIndex);
            if (destMana != null) {
                float destRoom = destMana.getMax() - destMana.get();
                converted = Math.min(converted, destRoom);
                if (converted > 0) {
                    destStatMap.addStatValue(manaIndex, converted);
                }
            }
        }

        state.addDrained(drainAmount);
        state.tick(dt);
        if (!drainSustain(dt, status)) return true;

        TransformComponent tc = buffer.getComponent(target, TransformComponent.getComponentType());
        if (tc != null) {
            DrainStyle.renderTick(tc.getPosition(), status.getHexContext(), buffer);
        }

        return false;
    }

    @Override
    public void onEnd(HexStatus<DrainState> status, ConstructTickContext ctx) {
        DrainState state = status.getState();
        if (state == null) return;

        cleanup(ctx, state.getEffectId());

        TransformComponent tc = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), TransformComponent.getComponentType());
        if (tc != null) {
            DrainStyle.renderComplete(tc.getPosition(), status.getHexContext(), ctx.getBuffer());
        }

        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());

        LOGGER.atInfo().log("drain: completed (%.2f drained)", state.getDrainedSoFar());
    }

    @Override
    public void onAbort(HexStatus<DrainState> status, ConstructTickContext ctx) {
        DrainState state = status.getState();
        cleanup(ctx, state != null ? state.getEffectId() : null);
        LOGGER.atInfo().log("drain: terminated early (%.2f drained); chain suppressed",
                state != null ? state.getDrainedSoFar() : 0f);
    }

    private static boolean effectRemoved(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> target, String effectId) {
        if (effectId == null) return false;
        int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
        if (effectIndex == Integer.MIN_VALUE) return false;
        EffectControllerComponent controller = buffer.getComponent(
                target, EffectControllerComponent.getComponentType());
        return controller == null || !controller.hasEffect(effectIndex);
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

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<DrainState> status) {
        DrainState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<DrainState> status, List<String> ids) {
        DrainState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }
}
