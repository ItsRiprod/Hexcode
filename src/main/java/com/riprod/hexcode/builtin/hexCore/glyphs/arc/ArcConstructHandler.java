package com.riprod.hexcode.builtin.hexCore.glyphs.arc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.arc.style.ArcStyle;
import com.riprod.hexcode.builtin.hexCore.glyphs.arc.utils.ArcUtils;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;

public class ArcConstructHandler implements ConstructHandler<ArcState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float SHOCK_OVERLAP = 0.25f;
    private static final String SHOCK_EFFECT_ID = "Hexcode_Shock";

    @Override
    public void onFirstTick(HexStatus<ArcState> status, ConstructTickContext ctx) {
        // shock the entity we just struck for the hop delay + overlap
        ArcState state = status.getState();
        if (state == null) return;
        ArcUtils.applyShockEffect(ctx.getBuffer(), ctx.getEntityRef(), state.getDelay() + SHOCK_OVERLAP);
    }

    @Override
    public boolean onTick(float dt, HexStatus<ArcState> status, ConstructTickContext ctx) {
        ArcState state = status.getState();
        if (state == null) return true;

        if (!state.hasFired()) {
            return fireCurrentBranch(state, status, ctx);
        }

        state.tick(dt);
        if (state.getElapsedSeconds() < state.getDelay()) return false;

        return hopToNext(state, status, ctx);
    }

    @Override
    public void onCleanup(HexStatus<ArcState> status, ConstructTickContext ctx) {
        // strip the shock effect off the target we were attached to
        Ref<EntityStore> target = ctx.getEntityRef();
        if (target != null && target.isValid()) {
            EffectControllerComponent controller = ctx.getBuffer().getComponent(
                    target, EffectControllerComponent.getComponentType());
            if (controller != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex(SHOCK_EFFECT_ID);
                if (effectIndex != Integer.MIN_VALUE) {
                    controller.removeEffect(target, effectIndex, ctx.getBuffer());
                }
            }
        }
    }

    private boolean fireCurrentBranch(ArcState state, HexStatus<ArcState> status,
            ConstructTickContext ctx) {
        String branch = state.getCurrentBranch();
        if (branch == null) return true;

        HexContext hexContext = status.getHexContext();
        if (!tryConsumePerHopVolatility(state, hexContext)) {
            return true;
        }

        Ref<EntityStore> targetRef = ctx.getEntityRef();
        UUIDComponent targetUuid = ctx.getBuffer().getComponent(
                targetRef, UUIDComponent.getComponentType());
        Glyph triggeringGlyph = status.getTriggeringGlyph();
        HexContext branchContext = hexContext.branch();
        branchContext.updateRuntimeAccessors(ctx.getBuffer());

        if (targetRef.isValid() && targetUuid != null && triggeringGlyph != null) {
            triggeringGlyph.writeOutput(
                    new EntityVar(targetUuid.getUuid(), targetRef), branchContext);
        }

        HexExecuter.continueExecution(List.of(branch), branchContext);

        state.advanceBranch();
        state.setHasFired(true);
        state.resetTimer();

        return !state.hasMoreBranches();
    }

    private boolean tryConsumePerHopVolatility(ArcState state, HexContext hexContext) {
        Glyph arcGlyph = state.getArcGlyph();
        if (arcGlyph == null) return true;

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(arcGlyph.getGlyphId());
        if (asset == null) return true;

        HexStats tracker = hexContext.getHexStats();
        if (tracker == null) return false;

        Impact impact = asset.getConfig() == null ? null : asset.getConfig().getVolatilityImpact();
        float finalCost = arcGlyph.computeBaseCost() * Impact.scale(impact, state.getMaxJumpDistance());

        if (hexContext.getHexRoot() == null) return true;
        return tracker.consumeVolatility(finalCost) > 0f;
    }

    private boolean hopToNext(ArcState state, HexStatus<ArcState> status, ConstructTickContext ctx) {
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Ref<EntityStore> currentRef = ctx.getEntityRef();

        TransformComponent fromTc = buffer.getComponent(
                currentRef, TransformComponent.getComponentType());
        if (fromTc == null) {
            LOGGER.atWarning().log("arc: current hop entity lost transform; ending chain");
            return true;
        }

        Vector3d fromPos = fromTc.getPosition();
        HexContext hexContext = status.getHexContext();

        Ref<EntityStore> nextTarget = ArcUtils.getNextArcTarget(
                fromPos, state.getMaxJumpDistance(), state.getVisited(), buffer);
        if (nextTarget == null) {
            LOGGER.atInfo().log("arc: no nearby unvisited entity within %.1f; ending chain",
                    state.getMaxJumpDistance());
            return true;
        }

        UUIDComponent nextUuid = buffer.getComponent(nextTarget, UUIDComponent.getComponentType());
        Set<UUID> nextVisited = new HashSet<>(state.getVisited());
        if (nextUuid != null) nextVisited.add(nextUuid.getUuid());

        TransformComponent nextTc = buffer.getComponent(nextTarget, TransformComponent.getComponentType());
        Vector3d nextPos = nextTc != null ? nextTc.getPosition() : fromPos;

        World world = buffer.getExternalData().getWorld();
        ArcStyle.renderArc(buffer, world, fromPos, nextPos, hexContext);
        ArcStyle.renderHit(buffer, nextPos, hexContext);

        List<String> remaining = new ArrayList<>(
                state.getBranches().subList(state.getBranchIndex(), state.getBranches().size()));

        ArcState nextState = new ArcState(state.getArcGlyph(), remaining, nextVisited,
                state.getMaxJumpDistance(), state.getDelay());

        HexConstructSpawner.applyWithState(
                buffer, nextTarget, hexContext, status.getTriggeringGlyph(),
                ArcGlyph.ID, nextState);

        return true;
    }
}
