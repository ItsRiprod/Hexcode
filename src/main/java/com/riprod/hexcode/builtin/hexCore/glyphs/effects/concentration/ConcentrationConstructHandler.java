package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import java.util.concurrent.TimeUnit;

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
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.style.ConcentrationStyle;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.cast.component.ManaSourceComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;
import com.riprod.hexcode.utils.LogScopes;

public class ConcentrationConstructHandler implements ConstructHandler<ConcentrationState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    @Override
    public boolean onTick(float dt, HexStatus<ConcentrationState> status, ConstructTickContext ctx) {
        Ref<EntityStore> casterRef = ctx.getEntityRef();
        if (casterRef == null || !casterRef.isValid())
            return true;

        var glyph = GlyphAsset.of(status.getTriggeringGlyph(), ConcentrationGlyph.class);
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        EntityStatMap statMap = buffer.getComponent(
                casterRef, EntityStatMap.getComponentType());
        EntityStatValue holdStat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding())
                : null;
        if (holdStat == null)
            return true;

        if (holdStat.get() < 1f) {
            fireRelease(glyph, status, buffer, casterRef);
            return true;
        }

        var tracker = status.getHexContext().volatility();
        if (tracker.getCurrent() <= 0f)
            return false;

        if (!payUpkeep(dt, glyph, status, statMap, buffer, casterRef)) {
            fireRelease(glyph, status, buffer, casterRef);
            return true;
        }

        accrueVolatility(dt, glyph, status);
        emitSecondary(dt, glyph, status, buffer, casterRef);

        drainSustain(dt, status);
        return false;
    }

    private void accrueVolatility(float dt, GlyphAsset glyph, HexStatus<ConcentrationState> status) {
        ConcentrationState state = status.getState();
        VolatilityComponent tracker = status.getHexContext().volatility();
        if (state == null)
            return;

        float elapsed = state.getElapsedSeconds() + dt;
        state.setElapsedSeconds(elapsed);

        float basePerSecond = glyph != null ? glyph.getVolatilityImpact().compute(elapsed) : 1.0f;
        tracker.add((basePerSecond + state.getBonusVolatilityPerSecond()) * dt);
    }

    private boolean payUpkeep(float dt, ConcentrationGlyph glyph, HexStatus<ConcentrationState> status,
            EntityStatMap statMap, CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        ConcentrationState state = status.getState();
        HexContext ctx = status.getHexContext();

        if (state == null || ctx == null || !ctx.policy().isConsumeMana())
            return true;

        var manaSource = ctx.get(ManaSourceComponent.TYPE);
        float mana = state.getManaRate() * dt;
        float stamina = state.getStaminaRate() * dt;

        if (mana > 0 && mana > manaSource.getCurrent(buffer))
            return false;
        if (!canAfford(statMap, DefaultEntityStatTypes.getStamina(), stamina))
            return false;

        if (mana > 0 && !manaSource.tryConsume(mana, buffer))
            return false;
        if (stamina > 0)
            statMap.subtractStatValue(DefaultEntityStatTypes.getStamina(), stamina);

        payHealthUpkeep(dt, glyph, state, buffer, casterRef);
        return true;
    }

    private static boolean canAfford(EntityStatMap statMap, int statIndex, float cost) {
        if (cost <= 0)
            return true;
        EntityStatValue stat = statMap.get(statIndex);
        return stat != null && stat.get() >= cost;
    }

    private void payHealthUpkeep(float dt, ConcentrationGlyph glyph, ConcentrationState state,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        if (state.getHealthRate() <= 0f)
            return;

        float accum = state.getHealthDamageAccum() + state.getHealthRate() * dt;
        int points = (int) accum;
        state.setHealthDamageAccum(accum - points);
        if (points <= 0 || glyph == null)
            return;

        DamageCause cause = DamageCause.getAssetMap().getAsset(glyph.getHealthDamageCauseId());
        if (cause == null) {
            LOGGER.atWarning().atMostEvery(5, TimeUnit.SECONDS)
                    .log("concentration: damage cause '%s' not found, health upkeep skipped",
                            glyph.getHealthDamageCauseId());
            return;
        }

        Damage damage = new Damage(new Damage.EntitySource(casterRef), cause, points);
        DamageSystems.executeDamage(casterRef, buffer, damage);

        if (damage.isCancelled()) {
            LOGGER.atFine().atMostEvery(5, TimeUnit.SECONDS)
                    .log("concentration: health upkeep of %d cancelled by the damage pipeline", points);
        }
    }

    private void fireRelease(ConcentrationGlyph glyph, HexStatus<ConcentrationState> status,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        Glyph trigger = status.getTriggeringGlyph();
        HexContext heldCtx = status.getHexContext();
        if (trigger == null || heldCtx == null)
            return;

        VolatilityComponent heldTracker = heldCtx.volatility();
        if (heldTracker.getCurrent() <= 0f)
            return;

        heldCtx.updateRuntimeAccessors(buffer);

        try {
            HexExecuter.branchFromSlot(trigger, ConcentrationGlyphSlots.RELEASE, heldCtx);
        } catch (Exception e) {
            LOGGER.atWarning().log("concentration: release fire failed: %s", e.getMessage());
        }

        TransformComponent casterTransform = buffer.getComponent(
                casterRef, TransformComponent.getComponentType());
        if (casterTransform != null) {
            ConcentrationStyle.renderEnd(glyph, casterTransform.getPosition(), heldCtx, buffer);
        }
    }

    private void emitSecondary(float dt, ConcentrationGlyph glyph, HexStatus<ConcentrationState> status,
            CommandBuffer<EntityStore> buffer, Ref<EntityStore> casterRef) {
        ConcentrationState state = status.getState();
        if (state == null || glyph == null)
            return;

        float interval = (float) glyph.getSecondaryIntervalSeconds();
        float accum = state.getTickAccum() + dt;
        while (accum >= interval) {
            accum -= interval;
            TransformComponent transform = buffer.getComponent(
                    casterRef, TransformComponent.getComponentType());
            if (transform == null)
                break;
            ConcentrationStyle.renderTick(glyph, transform.getPosition(), status.getHexContext(), buffer);
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

        status.getHexContext().endBranch();
    }
}
