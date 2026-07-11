package com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.component.MagicHealthComponent;

public class MageArmorConstructHandler implements ConstructHandler<MageArmorState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        MageArmorState state = status.getState();
        if (state == null) return true;
        state.tick(dt);
        if (!drainSustain(dt, status)) return true;
        if (isPoolDepleted(ctx)) return true;
        return state.isExpired();
    }

    private boolean isPoolDepleted(ConstructTickContext ctx) {
        Ref<EntityStore> ref = ctx.getEntityRef();
        if (ref == null || !ref.isValid()) return true;
        EntityStatMap statMap = ctx.getBuffer().getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return true;
        int statIndex = EntityStatType.getAssetMap().getIndex(MagicHealthComponent.STAT_ID);
        if (statIndex == Integer.MIN_VALUE) return false;
        EntityStatValue pool = statMap.get(statIndex);
        return pool == null || pool.get() <= 0f;
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

        EntityStatMap statMap = buffer.getComponent(targetRef, EntityStatMap.getComponentType());
        if (statMap != null) {
            int statIndex = EntityStatType.getAssetMap().getIndex(MagicHealthComponent.STAT_ID);
            if (statIndex != Integer.MIN_VALUE) {
                statMap.setStatValue(statIndex, 0f);
            }
        }

        buffer.tryRemoveComponent(targetRef, MagicHealthComponent.getComponentType());
    }
}
