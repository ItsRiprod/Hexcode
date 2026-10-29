package com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.utils.VfxUtil;

public class InvisibilityConstructHandler implements ConstructHandler<InvisibilityState> {

    @Override
    public boolean onTick(float dt, HexStatus<InvisibilityState> status, ConstructTickContext ctx) {
        InvisibilityState state = status.getState();
        if (state == null) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<InvisibilityState> status, ConstructTickContext ctx) {
        InvisibilityState state = status.getState();
        if (state == null) {
            cleanup(ctx, null);
            status.getHexContext().endBranch();
            return;
        }
        cleanup(ctx, state.getEffectId());
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
    }

    @Override
    public void onAbort(HexStatus<InvisibilityState> status, ConstructTickContext ctx) {
        // native effect outlives an early cancel unless we strip it here
        cleanup(ctx, status.getState() != null ? status.getState().getEffectId() : null);
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<InvisibilityState> status) {
        InvisibilityState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<InvisibilityState> status, List<String> ids) {
        InvisibilityState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(ConstructTickContext ctx, String effectId) {
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid()) return;

        HexAppearanceService.removeLayer(buffer, target, InvisibilityGlyph.TYPE);

        if (effectId == null) return;

        VfxUtil.removeEffect(buffer, target, effectId);
    }
}
