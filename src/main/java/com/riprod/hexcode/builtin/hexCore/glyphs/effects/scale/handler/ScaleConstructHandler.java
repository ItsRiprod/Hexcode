package com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.handler;

import java.util.List;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.ScaleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.components.ScaleStackComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.components.ScaleState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.style.ScaleStyle;

public class ScaleConstructHandler implements ConstructHandler<ScaleState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<ScaleState> status, ConstructTickContext ctx) {
        ScaleState state = status.getState();
        if (state == null) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<ScaleState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        ScaleState state = status.getState();
        if (state == null) return;
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atInfo().log("scale: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<ScaleState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atInfo().log("scale: terminated early; chain suppressed");
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<ScaleState> status) {
        ScaleState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<ScaleState> status, List<String> ids) {
        ScaleState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<ScaleState> status, ConstructTickContext ctx) {
        try {
            ScaleState state = status.getState();
            if (state == null) return;

            CommandBuffer<EntityStore> buffer = ctx.getBuffer();
            Ref<EntityStore> targetRef = ctx.getEntityRef();

            if (targetRef != null && targetRef.isValid()) {
                ScaleStackComponent stack = buffer.getComponent(
                        targetRef, ScaleStackComponent.getComponentType());
                if (stack != null) {
                    stack.remove(state.getConstructId());
                    String baseAssetId = stack.getBaseAssetId() != null
                            ? stack.getBaseAssetId()
                            : state.getModelAssetId();

                    if (stack.isEmpty()) {
                        ScaleGlyph.applyAbsoluteScale(buffer, targetRef, baseAssetId, 1.0f);
                        buffer.removeComponent(targetRef, ScaleStackComponent.getComponentType());
                    } else {
                        buffer.putComponent(targetRef, ScaleStackComponent.getComponentType(), stack);
                        float absolute = stack.productOfContributions();
                        ScaleGlyph.applyAbsoluteScale(buffer, targetRef, baseAssetId, absolute);
                    }

                    PlayerSkinComponent skinComp = buffer.getComponent(
                            targetRef, PlayerSkinComponent.getComponentType());
                    if (skinComp != null) {
                        skinComp.setNetworkOutdated();
                    }
                }

                TransformComponent tc = buffer.getComponent(
                        targetRef, TransformComponent.getComponentType());
                if (tc != null) {
                    ScaleStyle.renderRestore(tc.getPosition(), status.getHexContext(), buffer);
                }
            }

            Ref<EntityStore> visualRef = state.getVisualRef();
            if (visualRef != null && visualRef.isValid()) {
                buffer.tryRemoveEntity(visualRef, RemoveReason.REMOVE);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] ScaleConstructHandler cleanup failed: %s", e.getMessage());
        }
    }
}
