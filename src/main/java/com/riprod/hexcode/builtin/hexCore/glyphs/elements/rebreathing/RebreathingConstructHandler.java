package com.riprod.hexcode.builtin.hexCore.glyphs.elements.rebreathing;

import java.util.List;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class RebreathingConstructHandler implements ConstructHandler<RebreathingState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    @Override
    public boolean onTick(float dt, HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        RebreathingState state = status.getState();
        if (state == null) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        state.tick(dt);
        if (!drainSustain(dt, status)) return false;
        return state.isExpired();
    }

    @Override
    public void onEnd(HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        RebreathingState state = status.getState();
        if (state == null) {
            status.getHexContext().endBranch();
            return;
        }
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atFine().log("rebreathing: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atFine().log("rebreathing: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<RebreathingState> status) {
        RebreathingState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<RebreathingState> status, List<String> ids) {
        RebreathingState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<RebreathingState> status, ConstructTickContext ctx) {
        RebreathingState state = status.getState();
        if (state == null || state.isCleanedUp()) return;
        state.markCleanedUp();

        VfxUtil.removeEffect(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId());
    }
}
