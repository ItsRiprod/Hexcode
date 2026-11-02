package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward;

import java.util.List;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward.style.WardStyle;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.redirect.EntityRedirectSpawner;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;

public class WardConstructHandler implements ConstructHandler<WardState> {

    @Override
    public boolean onTick(float dt, HexStatus<WardState> status, ConstructTickContext ctx) {
        WardState state = status.getState();
        if (state == null) return true;

        Ref<EntityStore> casterRef = ctx.getEntityRef();
        if (casterRef == null || !casterRef.isValid()) return true;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        if (!resolves(state.getTargetRef(), buffer) || !resolves(state.getDeferralRef(), buffer)) return true;

        EntityStatMap statMap = buffer.getComponent(casterRef, EntityStatMap.getComponentType());
        EntityStatValue holdStat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding()) : null;
        if (holdStat == null || holdStat.get() < 1f) return true;

        VolatilityComponent tracker = status.getHexContext().volatility();

        float elapsed = state.getElapsedSeconds() + dt;
        state.setElapsedSeconds(elapsed);

        var asset = GlyphAsset.of(status.getTriggeringGlyph(), WardGlyph.class);
        var impact = asset != null ? asset.getSustainImpact() : null;
        float perSecond = impact != null ? impact.compute(elapsed) : WardGlyph.DEFAULT_SUSTAIN_PER_SECOND;
        tracker.add(perSecond * dt);

        renderWardLine(asset, state, buffer, status);

        return false;
    }

    private static boolean resolves(PersistentRef ref, CommandBuffer<EntityStore> buffer) {
        if (ref == null) return false;
        var entity = ref.getEntity(buffer);
        return entity != null && entity.isValid();
    }

    private void renderWardLine(WardGlyph asset, WardState state, CommandBuffer<EntityStore> buffer,
            HexStatus<WardState> status) {
        Ref<EntityStore> targetRef = state.getTargetRef() != null
                ? state.getTargetRef().getEntity(buffer) : null;
        Ref<EntityStore> deferralRef = state.getDeferralRef() != null
                ? state.getDeferralRef().getEntity(buffer) : null;
        if (targetRef == null || !targetRef.isValid() || deferralRef == null || !deferralRef.isValid())
            return;
        TransformComponent targetTransform = buffer.getComponent(targetRef, TransformComponent.getComponentType());
        TransformComponent deferralTransform = buffer.getComponent(deferralRef, TransformComponent.getComponentType());
        if (targetTransform == null || deferralTransform == null)
            return;
        WardStyle.renderWardLine(asset, new Vector3d(targetTransform.getPosition()),
                new Vector3d(deferralTransform.getPosition()), status.getHexContext(), buffer);
    }

    @Override
    public void onEnd(HexStatus<WardState> status, ConstructTickContext ctx) {
        WardState state = status.getState();
        cleanup(status, ctx);
        if (state == null) {
            status.getHexContext().endBranch();
            return;
        }

        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());

        TransformComponent casterTransform = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), TransformComponent.getComponentType());
        if (casterTransform != null) {
            var asset = GlyphAsset.of(status.getTriggeringGlyph(), WardGlyph.class);
            WardStyle.renderEnd(asset, casterTransform.getPosition(), status.getHexContext(), ctx.getBuffer());
        }
    }

    @Override
    public void onAbort(HexStatus<WardState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<WardState> status) {
        WardState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<WardState> status, List<String> ids) {
        WardState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<WardState> status, ConstructTickContext ctx) {
        WardState state = status.getState();
        if (state == null) return;
        Ref<EntityStore> targetRef = state.getTargetRef() != null
                ? state.getTargetRef().getEntity(ctx.getBuffer()) : null;
        EntityRedirectSpawner.unstamp(ctx.getBuffer(), targetRef, status.getConstructId());
    }
}
