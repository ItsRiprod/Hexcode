package com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze.component.FrozenBlock;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze.style.FreezeStyle;

public class FreezeConstructHandler implements ConstructHandler<FreezeState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<FreezeState> status, ConstructTickContext ctx) {
        FreezeState state = status.getState();
        if (state == null) return true;
        state.tick(dt);
        if (!drainSustain(dt, status)) return true;
        return state.isExpired();
    }

    @Override
    public void onEnd(HexStatus<FreezeState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        FreezeState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("freeze: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<FreezeState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("freeze: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<FreezeState> status) {
        FreezeState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<FreezeState> status, List<String> ids) {
        FreezeState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<FreezeState> status, ConstructTickContext ctx) {
        FreezeState state = status.getState();
        if (state == null) return;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> frozenRef = ctx.getEntityRef();

        String effectId = state.getEffectId();
        if (effectId != null && frozenRef != null && frozenRef.isValid()) {
            EffectControllerComponent controller = buffer.getComponent(
                    frozenRef, EffectControllerComponent.getComponentType());
            if (controller != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
                if (effectIndex != Integer.MIN_VALUE) {
                    controller.removeEffect(frozenRef, effectIndex, buffer);
                }
            }
        }

        World world = buffer.getExternalData().getWorld();
        for (FrozenBlock block : state.getFrozenBlocks()) {
            Vector3i pos = block.getPosition();
            Vector3d blockCenter = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
            world.setBlock(pos.x, pos.y, pos.z, block.getBlockTypeId());
            FreezeStyle.renderMelt(blockCenter, status.getHexContext(), buffer);
        }

        LOGGER.atInfo().log("freeze: cleanup restored %d blocks", state.getFrozenBlocks().size());
    }
}
