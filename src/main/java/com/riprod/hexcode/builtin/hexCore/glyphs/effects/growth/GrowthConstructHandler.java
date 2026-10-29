package com.riprod.hexcode.builtin.hexCore.glyphs.effects.growth;

import java.util.List;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class GrowthConstructHandler implements ConstructHandler<GrowthState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    @Override
    public boolean onTick(float dt, HexStatus<GrowthState> status, ConstructTickContext ctx) {
        GrowthState state = status.getState();
        if (state == null) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<GrowthState> status, ConstructTickContext ctx) {
        GrowthState state = status.getState();
        VfxUtil.removeEffect(ctx.getBuffer(), ctx.getEntityRef(), state != null ? state.getEffectId() : null);
        if (state == null) {
            status.getHexContext().endBranch();
            return;
        }
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atFine().log("growth: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<GrowthState> status, ConstructTickContext ctx) {
        GrowthState state = status.getState();
        VfxUtil.removeEffect(ctx.getBuffer(), ctx.getEntityRef(), state != null ? state.getEffectId() : null);
        LOGGER.atFine().log("growth: terminated early; chain suppressed");
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
}
