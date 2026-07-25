package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.style.ConcentrationStyle;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.execution.component.HexStats;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public class ConcentrationConstructHandler implements ConstructHandler<ConcentrationState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ConcentrationConfig resolveConfig(HexStatus<ConcentrationState> status) {
        Glyph trigger = status.getTriggeringGlyph();
        GlyphAsset asset = trigger != null
                ? GlyphAsset.getAssetMap().getAsset(trigger.getGlyphId()) : null;
        GlyphConfig raw = asset != null ? asset.getConfig() : null;
        return raw instanceof ConcentrationConfig cc ? cc : ConcentrationConfig.DEFAULTS;
    }

    @Override
    public boolean onTick(float dt, HexStatus<ConcentrationState> status, ConstructTickContext ctx) {
        Ref<EntityStore> casterRef = ctx.getEntityRef();
        if (casterRef == null || !casterRef.isValid())
            return true;

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        EntityStatMap statMap = buffer.getComponent(
                casterRef, EntityStatMap.getComponentType());
        EntityStatValue holdStat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding()) : null;
        if (holdStat == null)
            return true;

        if (holdStat.get() < 1f) {
            fireReleaseAndKillHeld(status, buffer, casterRef);
            return true;
        }

        if (chargeUpkeep(dt, status, buffer, casterRef)) {
            // starved of mana/resource: the channel ends here, same as a manual release
            fireReleaseAndKillHeld(status, buffer, casterRef);
            return true;
        }

        accrueVolatility(dt, status);
        emitSecondary(dt, status, buffer, casterRef);

        return !drainSustain(dt, status);
    }

    private void accrueVolatility(float dt, HexStatus<ConcentrationState> status) {
        ConcentrationState state = status.getState();
        HexStats tracker = status.getHexContext().getHexStats();
        if (state == null || tracker == null)
            return;

        float elapsed = state.getElapsedSeconds() + dt;
        state.setElapsedSeconds(elapsed);

        Impact impact = resolveConfig(status).getSustainImpact();
        float perSecond = impact != null ? impact.compute(elapsed) : ConcentrationConfig.DEFAULT_SUSTAIN_PER_SECOND;
        tracker.addVolatility(perSecond * dt);
    }

    // returns true when starvation should end the channel; false when upkeep was paid (or is inactive)
    private boolean chargeUpkeep(float dt, HexStatus<ConcentrationState> status,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        ConcentrationState state = status.getState();
        if (state == null || !state.isUpkeepActive())
            return false;
        HexContext ctx = status.getHexContext();
        if (ctx == null || !ctx.isConsumeMana())
            return false;

        ConcentrationConfig config = resolveConfig(status);
        double manaPerSecond = config.getManaPerSecond();
        double resourcePerSecond = config.getResourceDrainPerSecond();
        int resource = state.getResource();
        HexRoot root = ctx.getHexRoot();
        if (root == null)
            return false;

        float accum = state.getManaAccum() + dt;
        while (accum >= 1.0f) {
            accum -= 1.0f;
            if (manaPerSecond > 0 && !root.tryConsumeMana((float) manaPerSecond, buffer)) {
                state.setManaAccum(0f);
                return true;
            }
            if (resource != 0) {
                boolean paid = resource < 0
                        ? root.tryConsumeMana((float) resourcePerSecond, buffer)
                        : tryConsumeStat(buffer, casterRef, DefaultEntityStatTypes.getStamina(), (float) resourcePerSecond);
                if (!paid) {
                    state.setManaAccum(0f);
                    return true;
                }
            }
        }
        state.setManaAccum(accum);
        return false;
    }

    private boolean tryConsumeStat(CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef,
            int statIndex, float cost) {
        if (casterRef == null || !casterRef.isValid() || cost <= 0)
            return cost <= 0;
        EntityStatMap statMap = buffer.getComponent(casterRef, EntityStatMap.getComponentType());
        if (statMap == null)
            return false;
        EntityStatValue stat = statMap.get(statIndex);
        if (stat == null || stat.get() < cost)
            return false;
        statMap.subtractStatValue(statIndex, cost);
        return true;
    }

    private void fireReleaseAndKillHeld(HexStatus<ConcentrationState> status,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        Glyph trigger = status.getTriggeringGlyph();
        HexContext heldCtx = status.getHexContext();
        if (trigger == null || heldCtx == null)
            return;

        HexStats heldTracker = heldCtx.getHexStats();
        if (heldTracker != null && heldTracker.getCurrentVolatility() <= 0f)
            return;

        HexContext releaseCtx = HexContext.cloneState(heldCtx);
        releaseCtx.updateRuntimeAccessors(buffer);
        releaseCtx.beginRootBranch();

        CasterStateComponent idle = buffer.getComponent(
                casterRef, CasterStateComponent.getComponentType());
        if (idle != null) {
            idle.registerActiveTracker(releaseCtx.getHexStats());
        }

        if (heldTracker != null)
            heldTracker.setVolatility(0f);

        try {
            HexExecuter.continueFromSlot(trigger, ConcentrationGlyphSlots.RELEASE, releaseCtx);
        } catch (Exception e) {
            LOGGER.atWarning().log("concentration: cleanup fire failed: %s", e.getMessage());
        }

        TransformComponent casterTransform = buffer.getComponent(
                casterRef, TransformComponent.getComponentType());
        if (casterTransform != null) {
            ConcentrationStyle.renderEnd(casterTransform.getPosition(), releaseCtx, buffer);
        }
    }

    private void emitSecondary(float dt, HexStatus<ConcentrationState> status,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        ConcentrationState state = status.getState();
        if (state == null)
            return;

        float interval = (float) resolveConfig(status).getSecondaryIntervalSeconds();
        float accum = state.getTickAccum() + dt;
        while (accum >= interval) {
            accum -= interval;
            TransformComponent transform = buffer.getComponent(
                    casterRef, TransformComponent.getComponentType());
            if (transform == null)
                break;
            ConcentrationStyle.renderTick(transform.getPosition(), status.getHexContext(), buffer);
        }
        state.setTickAccum(accum);
    }

    @Override
    public void onCleanup(HexStatus<ConcentrationState> status, ConstructTickContext ctx) {
        ConcentrationState state = status.getState();
        if (state != null) {
            Ref<EntityStore> visualRef = state.getVisualRef();
            if (visualRef != null && visualRef.isValid()) {
                ctx.getBuffer().tryRemoveEntity(visualRef, RemoveReason.REMOVE);
            }
        }

        HexExecuter.fail(status.getHexContext());
    }
}
