package com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.handler;

import java.util.List;
import java.util.UUID;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.ScaleGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.components.ScaleState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.style.ScaleStyle;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class ScaleConstructHandler implements ConstructHandler<ScaleState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    @Override
    public boolean onTick(float dt, HexStatus<ScaleState> status, ConstructTickContext ctx) {
        ScaleState state = status.getState();
        if (state == null) return true;
        if (VfxUtil.isEffectRemoved(ctx.getBuffer(), ctx.getEntityRef(), state.getEffectId())) return true;
        if (state.isExpired()) return true;
        state.tick(dt);
        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<ScaleState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        ScaleState state = status.getState();
        if (state == null) {
            HexExecuter.endBranch(status.getHexContext());
            return;
        }
        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());
        LOGGER.atFine().log("scale: ended, firing %d next glyphs", state.getNextGlyphIds().size());
    }

    @Override
    public void onAbort(HexStatus<ScaleState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        LOGGER.atFine().log("scale: terminated early; chain suppressed");
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
                HexAppearanceService.removeLayer(buffer, targetRef, state.getConstructId().toString());
                releaseEffect(buffer, targetRef, state.getEffectId(), status.getConstructId());

                TransformComponent tc = buffer.getComponent(
                        targetRef, TransformComponent.getComponentType());
                if (tc != null) {
                    var asset = GlyphAsset.of(status.getTriggeringGlyph(), ScaleGlyph.class);
                    ScaleStyle.renderRestore(asset, tc.getPosition(), status.getHexContext(), buffer);
                }
            }

            Ref<EntityStore> visualRef = state.getVisualRef();
            if (visualRef != null && visualRef.isValid()) {
                buffer.tryRemoveEntity(visualRef, RemoveReason.REMOVE);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("ScaleConstructHandler cleanup failed: %s", e.getMessage());
        }
    }

    public static void releaseEffect(CommandBuffer<EntityStore> buffer, Ref<EntityStore> target,
            String effectId, UUID excludeConstructId) {
        if (effectId == null || othersRemain(buffer, target, excludeConstructId)) return;
        VfxUtil.removeEffect(buffer, target, effectId);
    }

    private static boolean othersRemain(CommandBuffer<EntityStore> buffer, Ref<EntityStore> target,
            UUID excludeConstructId) {
        var found = new boolean[1];
        ConstructStateUtil.forEachStatus(buffer, target, ScaleGlyph.TYPE, (id, other) -> {
            if (!id.equals(excludeConstructId)) found[0] = true;
        });
        return found[0] || HexConstructSpawner.hasPendingApply(buffer, target, ScaleGlyph.TYPE);
    }
}
