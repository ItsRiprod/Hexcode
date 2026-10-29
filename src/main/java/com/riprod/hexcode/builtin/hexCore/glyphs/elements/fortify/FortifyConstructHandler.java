package com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify.component.FortifyWardComponent;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.utils.VfxUtil;

public class FortifyConstructHandler implements ConstructHandler<FortifyState> {

    @Override
    public boolean onTick(float dt, HexStatus<FortifyState> status, ConstructTickContext ctx) {
        FortifyState state = status.getState();
        if (state == null) return true;
        if (state.isConsumed()) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<FortifyState> status, ConstructTickContext ctx) {
        FortifyState state = status.getState();
        if (state == null) {
            status.getHexContext().endBranch();
            return;
        }
        cleanup(ctx, state.getEffectId());
        HexContext hexContext = status.getHexContext();
        hexContext.updateRuntimeAccessors(ctx.getBuffer());

        EntityVar attacker = state.getAttacker();
        if (attacker != null) {
            Glyph trigger = status.getTriggeringGlyph();
            if (trigger != null) {
                trigger.writeOutput(attacker, hexContext);
            } else {
                hexContext.setDefaultVariable(attacker);
            }
        }

        HexExecuter.continueExecution(state.getNextGlyphIds(), hexContext);
    }

    @Override
    public void onAbort(HexStatus<FortifyState> status, ConstructTickContext ctx) {
        // native effect outlives an early cancel unless we strip it here
        cleanup(ctx, status.getState() != null ? status.getState().getEffectId() : null);
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<FortifyState> status) {
        FortifyState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<FortifyState> status, List<String> ids) {
        FortifyState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(ConstructTickContext ctx, String effectId) {
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target == null || !target.isValid()) return;

        buffer.tryRemoveComponent(target, FortifyWardComponent.getComponentType());

        if (effectId == null) return;
        VfxUtil.removeEffect(buffer, target, effectId);
    }
}
