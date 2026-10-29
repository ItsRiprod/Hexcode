package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class MageArmorConstructHandler implements ConstructHandler<MageArmorState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    @Override
    public boolean onTick(float dt, HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        MageArmorState state = status.getState();
        if (state == null) return true;
        if (state.isPoolBroken()) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        state.tick(dt);
        if (state.isExpired()) return true;
        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        MageArmorState state = status.getState();
        if (state == null) {
            status.getHexContext().endBranch();
            return;
        }
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atFine().log("magearmor: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<MageArmorState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atFine().log("magearmor: terminated early; chain suppressed");
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
            VfxUtil.removeEffect(buffer, targetRef, effectId);
        }

        buffer.tryRemoveComponent(targetRef, MagicHealthComponent.getComponentType());
    }
}
