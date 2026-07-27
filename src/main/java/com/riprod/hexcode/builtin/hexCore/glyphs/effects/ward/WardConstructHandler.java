package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward;

import java.util.List;
import java.util.UUID;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ward.style.WardStyle;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.execution.cast.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.redirect.EntityRedirectSpawner;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;

public class WardConstructHandler implements ConstructHandler<WardState> {

    private WardConfig resolveConfig(HexStatus<WardState> status) {
        Glyph trigger = status.getTriggeringGlyph();
        GlyphAsset asset = trigger != null
                ? GlyphAsset.getAssetMap().getAsset(trigger.getGlyphId()) : null;
        GlyphConfig raw = asset != null ? asset.getConfig() : null;
        return raw instanceof WardConfig wc ? wc : WardConfig.DEFAULTS;
    }

    @Override
    public boolean onTick(float dt, HexStatus<WardState> status, ConstructTickContext ctx) {
        WardState state = status.getState();
        if (state == null) return true;

        Ref<EntityStore> casterRef = ctx.getEntityRef();
        if (casterRef == null || !casterRef.isValid()) return true;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        EntityStatMap statMap = buffer.getComponent(casterRef, EntityStatMap.getComponentType());
        EntityStatValue holdStat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding()) : null;
        if (holdStat == null || holdStat.get() < 1f) return true;

        VolatilityComponent tracker = status.getHexContext().volatility();
        if (tracker == null) return true;

        float elapsed = state.getElapsedSeconds() + dt;
        state.setElapsedSeconds(elapsed);

        Impact impact = resolveConfig(status).getSustainImpact();
        float perSecond = impact != null ? impact.compute(elapsed) : WardConfig.DEFAULT_SUSTAIN_PER_SECOND;
        tracker.add(perSecond * dt);

        renderWardLine(state, buffer, status);

        return tracker.getCurrent() <= 0f;
    }

    private void renderWardLine(WardState state, CommandBuffer<EntityStore> buffer,
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
        WardStyle.renderWardLine(new Vector3d(targetTransform.getPosition()),
                new Vector3d(deferralTransform.getPosition()), status.getHexContext(), buffer);
    }

    @Override
    public void onEnd(HexStatus<WardState> status, ConstructTickContext ctx) {
        WardState state = status.getState();
        cleanup(state, ctx);
        if (state == null) return;

        status.getHexContext().updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), status.getHexContext());

        TransformComponent casterTransform = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), TransformComponent.getComponentType());
        if (casterTransform != null) {
            WardStyle.renderEnd(casterTransform.getPosition(), status.getHexContext(), ctx.getBuffer());
        }
    }

    @Override
    public void onAbort(HexStatus<WardState> status, ConstructTickContext ctx) {
        cleanup(status.getState(), ctx);
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

    private void cleanup(WardState state, ConstructTickContext ctx) {
        if (state == null) return;
        Ref<EntityStore> targetRef = state.getTargetRef() != null
                ? state.getTargetRef().getEntity(ctx.getBuffer()) : null;
        UUID deferralUuid = state.getDeferralRef() != null ? state.getDeferralRef().getUuid() : null;
        EntityRedirectSpawner.unstamp(ctx.getBuffer(), targetRef, deferralUuid);
    }
}
