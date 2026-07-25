package com.riprod.hexcode.builtin.hexCore.glyphs.effects.illuminate;

import com.hypixel.hytale.component.CommandBuffer;
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

        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        IlluminateState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
    }

    @Override
    public void onAbort(HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
    }

    private void cleanup(HexStatus<IlluminateState> status, ConstructTickContext ctx) {
        IlluminateState state = status.getState();
        if (state == null) return;
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();

        if (state.isShowBox()) {
            GlowUtil.removeBox(buffer, state.getVolumeId());
        }

        if (state.isSpawnedOwner()) {
            CleanupUtils.safeRemoveEntity(buffer, ctx.getEntityRef());
        } else {
            buffer.tryRemoveComponent(ctx.getEntityRef(), DynamicLight.getComponentType());
        }
    }
}
