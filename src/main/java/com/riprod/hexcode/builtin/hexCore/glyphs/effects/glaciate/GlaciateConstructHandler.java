package com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.style.GlaciateStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;

public class GlaciateConstructHandler implements ConstructHandler<GlaciateState> {

    private static int damageCauseIndex = -1;

    @Override
    public void onFirstTick(HexStatus<GlaciateState> status, ConstructTickContext ctx) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering == null)
            return;
        Slot immediate = triggering.getSlot(GlaciateGlyphSlots.IMMEDIATE);
        if (immediate == null)
            return;
        String[] links = immediate.getLinks();
        if (links == null || links.length == 0)
            return;
        HexContext hexContext = status.getHexContext();
        hexContext.updateRuntimeAccessors(ctx.getBuffer());
        UUID entityId = ctx.getBuffer().getComponent(ctx.getEntityRef(), UUIDComponent.getComponentType())
                .getUuid();

        hexContext.setDefaultVariable(new EntityVar(entityId, ctx.getEntityRef()));
        HexExecuter.continueExecution(Arrays.asList(links), hexContext);
    }

    @Override
    public boolean onTick(float dt, HexStatus<GlaciateState> status, ConstructTickContext ctx) {
        GlaciateComponent glaciate = ctx.getChunk().getComponent(
                ctx.getIndex(), GlaciateComponent.getComponentType());
        TransformComponent transform = ctx.getChunk().getComponent(
                ctx.getIndex(), TransformComponent.getComponentType());
        if (glaciate == null || transform == null)
            return true;

        if (!glaciate.tickDuration(dt)) {
            return true;
        }

        Velocity vel = ctx.getChunk().getComponent(ctx.getIndex(), Velocity.getComponentType());
        Vector3d iceVelocity = vel != null ? new Vector3d(vel.getVelocity()) : new Vector3d();
        double speed = vel != null ? vel.getSpeed() : 0;

        Vector3d center = transform.getPosition();
        List<Ref<EntityStore>> found = TargetUtil.getAllEntitiesInSphere(
                center, glaciate.getDamageRadius(), ctx.getBuffer());

        Ref<EntityStore> casterRef = status.getHexContext().getCasterRef(ctx.getBuffer());
        GlaciateState state = status.getState();
        List<String> nextLinks = state != null ? state.getNextGlyphIds() : List.of();

        GlaciateConfig config = resolveConfig(status.getTriggeringGlyph());

        for (Ref<EntityStore> ref : found) {
            if (ref == null || !ref.isValid())
                continue;
            if (ref.equals(ctx.getEntityRef()))
                continue;
            if (ref.equals(casterRef))
                continue;
            if (ctx.getBuffer().getComponent(ref, GlaciateComponent.getComponentType()) != null)
                continue;

            UUIDComponent uuid = ctx.getBuffer().getComponent(ref, UUIDComponent.getComponentType());
            if (uuid == null)
                continue;

            if (glaciate.getHitEntities().contains(uuid.getUuid()))
                continue;
            glaciate.getHitEntities().add(uuid.getUuid());

            if (speed > config.getMinDamageSpeed()) {
                float damage = (float) (speed * glaciate.getDamageMultiplier());
                damage *= status.getHexContext().getMagicPowerMultiplier();
                applyDamage(ref, damage, ctx);
                applyKnockback(ref, iceVelocity, speed, config, ctx);
            }

            GlaciateStyle.renderImpact(center,
                    status.getHexContext(), ctx.getBuffer());

            Glyph triggering = status.getTriggeringGlyph();
            if (triggering != null && !nextLinks.isEmpty()) {
                HexContext hexCtx = status.getHexContext().branch();
                hexCtx.updateRuntimeAccessors(ctx.getBuffer());
                triggering.writeDefaultOutput(
                        new EntityVar(uuid.getUuid(), ref), hexCtx);
                HexExecuter.continueExecution(nextLinks, hexCtx);
            }
        }

        return !drainSustain(dt, status);
    }

    @Override
    public void onEnd(HexStatus<GlaciateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        GlaciateState state = status.getState();
        if (state == null) return;
        HexContext hexContext = status.getHexContext();
        hexContext.updateRuntimeAccessors(ctx.getBuffer());
        HexExecuter.continueExecution(state.getNextGlyphIds(), hexContext);
    }

    @Override
    public void onAbort(HexStatus<GlaciateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<GlaciateState> status) {
        GlaciateState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<GlaciateState> status, List<String> ids) {
        GlaciateState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void cleanup(HexStatus<GlaciateState> status, ConstructTickContext ctx) {
        TransformComponent transform = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), TransformComponent.getComponentType());
        if (transform != null) {
            GlaciateStyle.renderMelt(transform.getPosition(),
                    status.getHexContext(), ctx.getBuffer());
        }
        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }

    private void applyDamage(Ref<EntityStore> targetRef, float amount, ConstructTickContext ctx) {
        if (damageCauseIndex < 0) {
            damageCauseIndex = DamageCause.getAssetMap().getIndex("Environment");
        }
        if (damageCauseIndex == Integer.MIN_VALUE)
            return;

        DamageCause cause = DamageCause.getAssetMap().getAsset(damageCauseIndex);
        if (cause == null)
            return;

        Damage damage = new Damage(
                new Damage.EnvironmentSource("hex_glaciate"), cause, amount);
        DamageSystems.executeDamage(targetRef, ctx.getBuffer(), damage);
    }

    private void applyKnockback(Ref<EntityStore> ref, Vector3d iceVelocity, double speed,
            GlaciateConfig config, ConstructTickContext ctx) {
        Vector3d kbVelocity = new Vector3d(iceVelocity).normalize().mul(speed * config.getKnockbackScale());
        kbVelocity.y = Math.max(kbVelocity.y, config.getMinVerticalKnockback());
        KnockbackComponent kb = new KnockbackComponent();
        kb.setVelocity(kbVelocity);
        kb.setVelocityType(ChangeVelocityType.Add);
        kb.setDuration(0.0f);
        ctx.getBuffer().putComponent(ref, KnockbackComponent.getComponentType(), kb);
    }

    private GlaciateConfig resolveConfig(Glyph triggeringGlyph) {
        if (triggeringGlyph == null) return GlaciateConfig.DEFAULTS;
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(triggeringGlyph.getGlyphId());
        if (asset == null) return GlaciateConfig.DEFAULTS;
        return asset.getConfig() instanceof GlaciateConfig gc ? gc : GlaciateConfig.DEFAULTS;
    }
}
