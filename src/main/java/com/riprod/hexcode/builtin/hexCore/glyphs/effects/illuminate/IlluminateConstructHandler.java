package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate.utils.GlowUtil;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.utils.CleanupUtils;

public class IlluminateConstructHandler implements ConstructHandler<IlluminateState> {

    @Override
    public void onFirstTick(HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        IlluminateState state = status.getState();
        if (state != null && state.isShowBox()) {
            GlowUtil.broadcastBox(ctx.getBuffer(), ctx.getEntityRef(), state);
        }
    }

    @Override
    public boolean onTick(float dt, HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        IlluminateState state = status.getState();
        if (state == null) return true;

        state.tick(dt);
        if (state.isExpired()) return true;

        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        IlluminateState state = status.getState();
        if (state == null) {
            HexExecuter.endBranch(status.getHexContext());
            return;
        }
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
    }

    @Override
    public void onAbort(HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<IlluminateState> status) {
        var state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<IlluminateState> status, List<String> ids) {
        var state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        IlluminateState state = status.getState();
        if (state == null) return;
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();

        if (state.isShowBox()) {
            GlowUtil.removeBox(buffer, state.getVolumeId());
        }

        var target = ctx.getEntityRef();
        if (state.isSpawnedOwner()) {
            CleanupUtils.safeRemoveEntity(buffer, target);
        } else if (target != null && target.isValid()) {
            var previousLight = state.getPreviousLight();
            if (previousLight != null) {
                buffer.putComponent(target, DynamicLight.getComponentType(),
                        new DynamicLight(new ColorLight(previousLight)));
            } else {
                buffer.tryRemoveComponent(target, DynamicLight.getComponentType());
            }
        }
    }
}
