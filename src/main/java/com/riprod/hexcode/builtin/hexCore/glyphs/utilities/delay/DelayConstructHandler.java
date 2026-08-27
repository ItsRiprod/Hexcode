package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay;

import java.util.Arrays;
import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.delay.style.DelayStyle;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;

public class DelayConstructHandler implements ConstructHandler<DelayState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void onFirstTick(HexStatus<DelayState> status, ConstructTickContext ctx) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering == null)
            return;
        Slot immediate = triggering.getSlot(DelayGlyphSlots.IMMEDIATE);
        if (immediate == null)
            return;
        String[] links = immediate.getLinks();
        if (links == null || links.length == 0)
            return;
        HexContext hexContext = status.getHexContext();
        hexContext.updateRuntimeAccessors(ctx.getBuffer());
        HexContext immediateCtx = hexContext.branch();
        DelayState state = status.getState();
        if (state != null && state.isCustom()) {
            UUIDComponent uuidComponent = ctx.getBuffer().getComponent(
                    ctx.getEntityRef(), UUIDComponent.getComponentType());
            if (uuidComponent != null) {
                immediateCtx.setDefaultVariable(new EntityVar(uuidComponent.getUuid(), ctx.getEntityRef()));
            }
        }
        HexExecuter.continueExecution(Arrays.asList(links), immediateCtx);
    }

    @Override
    public boolean onTick(float dt, HexStatus<DelayState> status, ConstructTickContext ctx) {
        DelayState state = status.getState();
        if (state == null) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<DelayState> status, ConstructTickContext ctx) {
        DelayState state = status.getState();
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();

        if (state != null) {
            TransformComponent tc = buffer.getComponent(
                    ctx.getEntityRef(), TransformComponent.getComponentType());
            if (tc != null) {
                DelayStyle.renderExpiry(tc.getPosition(), status.getHexContext(), buffer);
            }
            status.getHexContext().updateRuntimeAccessors(buffer);
            HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        }

        onCleanup(status, ctx);
    }
    
    @Override
    public void onCleanup(HexStatus<DelayState> status, ConstructTickContext ctx) {

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();

        DelayState state = status.getState();
        if (state != null && state.isCustom()) {
            buffer.tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
        }
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<DelayState> status) {
        DelayState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<DelayState> status, List<String> ids) {
        DelayState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }
}
