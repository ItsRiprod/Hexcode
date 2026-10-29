package com.riprod.hexcode.builtin.hexCore.glyphs.elements.electrocute;

import java.util.List;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class ElectrocuteConstructHandler implements ConstructHandler<ElectrocuteState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    @Override
    public boolean onTick(float dt, HexStatus<ElectrocuteState> status, ConstructTickContext ctx) {
        ElectrocuteState state = status.getState();
        if (state == null) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        state.tick(dt);
        if (!drainSustain(dt, status)) return false;
        return state.isExpired();
    }

    @Override
    public void onEnd(HexStatus<ElectrocuteState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        ElectrocuteState state = status.getState();
        if (state == null) {
            status.getHexContext().endBranch();
            return;
        }
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atFine().log("electrocute: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<ElectrocuteState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atFine().log("electrocute: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<ElectrocuteState> status) {
        ElectrocuteState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<ElectrocuteState> status, List<String> ids) {
        ElectrocuteState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<ElectrocuteState> status, ConstructTickContext ctx) {
        ElectrocuteState state = status.getState();
        if (state == null || state.isCleanedUp()) return;
        state.markCleanedUp();

        VfxUtil.removeEffect(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId());
    }
}
