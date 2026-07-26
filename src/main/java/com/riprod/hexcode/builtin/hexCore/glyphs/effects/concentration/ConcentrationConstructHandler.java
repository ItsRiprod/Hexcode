package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
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

        if (!payUpkeep(dt, status, statMap, buffer, casterRef)) {
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
        float basePerSecond = impact != null ? impact.compute(elapsed) : ConcentrationConfig.DEFAULT_SUSTAIN_PER_SECOND;
        tracker.addVolatility((basePerSecond + state.getBonusVolatilityPerSecond()) * dt);
    }

    private boolean payUpkeep(float dt, HexStatus<ConcentrationState> status, EntityStatMap statMap,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        ConcentrationState state = status.getState();
        HexContext ctx = status.getHexContext();
        if (state == null || ctx == null || !ctx.isConsumeMana())
            return true;

        HexRoot root = ctx.getHexRoot();
        if (root == null)
            return true;

        float mana = state.getManaRate() * dt;
        float stamina = state.getStaminaRate() * dt;

        // every stat is checked before any is spent so a starved tick never charges for
        // volatility it will not grant
        if (mana > root.getCurrentMana(buffer) || !canAfford(statMap, DefaultEntityStatTypes.getStamina(), stamina))
            return false;

        if (mana > 0 && !root.tryConsumeMana(mana, buffer))
            return false;
        if (stamina > 0)
            statMap.subtractStatValue(DefaultEntityStatTypes.getStamina(), stamina);

        payHealthUpkeep(dt, state, resolveConfig(status), buffer, casterRef);
        return true;
    }

    private static boolean canAfford(EntityStatMap statMap, int statIndex, float cost) {
        if (cost <= 0)
            return true;
        EntityStatValue stat = statMap.get(statIndex);
        return stat != null && stat.get() >= cost;
    }

    // health is spent through the damage pipeline rather than the stat map, because only
    // damage marks the caster dead when it empties the bar
    private void payHealthUpkeep(float dt, ConcentrationState state, ConcentrationConfig config,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        if (state.getHealthRate() <= 0f)
            return;

        float accum = state.getHealthDamageAccum() + state.getHealthRate() * dt;
        int points = (int) accum;
        state.setHealthDamageAccum(accum - points);
        if (points <= 0)
            return;

        DamageCause cause = DamageCause.getAssetMap().getAsset(config.getHealthDamageCauseId());
        if (cause == null)
            return;

        DamageSystems.executeDamage(casterRef, buffer,
                new Damage(new Damage.EntitySource(casterRef), cause, points));
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
