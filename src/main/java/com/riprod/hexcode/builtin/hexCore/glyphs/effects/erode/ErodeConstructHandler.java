package com.riprod.hexcode.builtin.hexCore.glyphs.effects.erode;

import java.util.List;

import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.utils.VfxUtil;

public class ErodeConstructHandler implements ConstructHandler<ErodeState> {

    @Override
    public boolean onTick(float dt, HexStatus<ErodeState> status, ConstructTickContext ctx) {
        ErodeState state = status.getState();
        if (state == null) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<ErodeState> status, ConstructTickContext ctx) {
        ErodeState state = status.getState();
        if (state == null) {
            HexExecuter.endBranch(status.getHexContext());
            return;
        }
        VfxUtil.removeEffect(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId());
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
    }

    @Override
    public void onAbort(HexStatus<ErodeState> status, ConstructTickContext ctx) {
        // native effect outlives an early cancel unless we strip it here
        VfxUtil.removeEffect(ctx.getBuffer(), ctx.getEntityRef(), status.getState() != null ? status.getState().getEffectId() : null);
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<ErodeState> status) {
        ErodeState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<ErodeState> status, List<String> ids) {
        ErodeState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }
}
