package com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate;

import java.util.Arrays;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
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
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate.component.GlaciateComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.utils.VelocityUtil;

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
        UUIDComponent uuidComponent = ctx.getBuffer().getComponent(ctx.getEntityRef(), UUIDComponent.getComponentType());
        if (uuidComponent == null)
            return;
        HexContext hexContext = status.getHexContext();
        hexContext.updateRuntimeAccessors(ctx.getBuffer());

        HexContext immediateCtx = hexContext.branch();
        immediateCtx.setDefaultVariable(new EntityVar(uuidComponent.getUuid(), ctx.getEntityRef()));
        HexExecuter.continueExecution(Arrays.asList(links), immediateCtx);
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
        List<Ref<EntityStore>> found = new ObjectArrayList<>(TargetUtil.getAllEntitiesInSphere(
                center, glaciate.getDamageRadius(), ctx.getBuffer()));

        Ref<EntityStore> casterRef = status.getHexContext().getRootRef();
        GlaciateState state = status.getState();
        List<String> nextLinks = state != null ? state.getNextGlyphIds() : List.of();

        var config = GlyphAsset.of(status.getTriggeringGlyph(), GlaciateGlyph.class);

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

            if (config != null && speed > config.getMinDamageSpeed()) {
                float damage = (float) (speed * glaciate.getDamageMultiplier());
                applyDamage(ref, damage, ctx, casterRef);
                applyKnockback(ref, iceVelocity, speed, config, ctx);
            }

            EffectRenderer.render(EffectSlot.Hit, config, status.getHexContext(), center, ctx.getBuffer());

            Glyph triggering = status.getTriggeringGlyph();
            if (triggering != null && !nextLinks.isEmpty()) {
                HexContext hexCtx = status.getHexContext().branch();
                hexCtx.updateRuntimeAccessors(ctx.getBuffer());
                triggering.writeDefaultOutput(
                        new EntityVar(uuid.getUuid(), ref), hexCtx);
                HexExecuter.continueExecution(nextLinks, hexCtx);
            }
        }

        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<GlaciateState> status, ConstructTickContext ctx) {
        cleanup(status, ctx);
        GlaciateState state = status.getState();
        if (state == null) {
            HexExecuter.endBranch(status.getHexContext());
            return;
        }
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
            EffectRenderer.render(EffectSlot.End, GlyphAsset.of(status.getTriggeringGlyph(), GlaciateGlyph.class),
                    status.getHexContext(), transform.getPosition(), ctx.getBuffer());
        }
        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }

    private void applyDamage(Ref<EntityStore> targetRef, float amount, ConstructTickContext ctx, Ref<EntityStore> casterRef) {
        if (damageCauseIndex < 0) {
            damageCauseIndex = DamageCause.getAssetMap().getIndex("Environment");
        }
        if (damageCauseIndex == Integer.MIN_VALUE)
            return;

        DamageCause cause = DamageCause.getAssetMap().getAsset(damageCauseIndex);
        if (cause == null)
            return;

        Damage damage = new Damage(
                new Damage.EntitySource(casterRef), cause, amount);
        DamageSystems.executeDamage(targetRef, ctx.getBuffer(), damage);
    }

    private void applyKnockback(Ref<EntityStore> ref, Vector3d iceVelocity, double speed,
            GlaciateGlyph config, ConstructTickContext ctx) {
        Vector3d kbVelocity = new Vector3d(iceVelocity).normalize().mul(speed * config.getKnockbackScale());
        kbVelocity.y = Math.max(kbVelocity.y, config.getMinVerticalKnockback());
        VelocityUtil.knockback(ref, kbVelocity, ChangeVelocityType.Add, null, 0.0f, ctx.getBuffer());
    }
}
