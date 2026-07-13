package com.riprod.hexcode.builtin.glyphs.ensnare;

import java.util.List;
import java.util.UUID;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.builtin.glyphs.ensnare.component.EnsnareComponent;
import com.riprod.hexcode.builtin.glyphs.ensnare.component.SpikeEntry;
import com.riprod.hexcode.builtin.glyphs.ensnare.style.EnsnareStyle;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.state.NoState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.utils.VfxUtil;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

public class EnsnareConstructHandler implements ConstructHandler<NoState> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final double SPIKE_HIT_RADIUS_SQ = 0.7 * 0.7;
    private static int damageCauseIndex = -1;

    @Override
    public boolean onTick(float dt, HexStatus<NoState> status, ConstructTickContext ctx) {
        EnsnareComponent ensnare = ctx.getChunk().getComponent(
                ctx.getIndex(), EnsnareComponent.getComponentType());
        if (ensnare == null) return true;

        ensnare.incrementElapsed(dt);
        if (ensnare.getElapsedSeconds() >= ensnare.getDurationSeconds()) return true;

        processDamage(ensnare, status, ctx, status.getHexContext().getCasterRef());
        return !drainSustain(dt, status);
    }

    @Override
    public void onCleanup(HexStatus<NoState> status, ConstructTickContext ctx) {
        EnsnareComponent ensnare = ctx.getChunk().getComponent(
                ctx.getIndex(), EnsnareComponent.getComponentType());
        if (ensnare != null) {
            removeSpikes(ensnare, status, ctx.getBuffer());
            LOGGER.atFine().log("ensnare: expired after %.1fs, removed %d spikes",
                    ensnare.getDurationSeconds(), ensnare.getSpikes().size());
        }

        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }

    private void processDamage(EnsnareComponent ensnare, HexStatus<NoState> status,
            ConstructTickContext ctx, Ref<EntityStore> casterRef) {
        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Vector3d center = ensnare.getCenter();
        double radius = ensnare.getRadius() + 1.0;
        Vector3d min = new Vector3d(center.x - radius, center.y - 3, center.z - radius);
        Vector3d max = new Vector3d(center.x + radius, center.y + 4, center.z + radius);

        List<Ref<EntityStore>> nearbyEntitiesScratch = TargetUtil.getAllEntitiesInBox(min, max, buffer);
        var nearbyEntities = new ReferenceArrayList<>(nearbyEntitiesScratch);
        if (nearbyEntities.isEmpty()) return;


        for (Ref<EntityStore> targetRef : nearbyEntities) {
            if (targetRef == null || !targetRef.isValid()) continue;

            UUIDComponent uuidComp = buffer.getComponent(targetRef, UUIDComponent.getComponentType());
            if (uuidComp == null) continue;

            UUID targetId = uuidComp.getUuid();
            if (!ensnare.canDamageTarget(targetId)) continue;

            TransformComponent tc = buffer.getComponent(targetRef, TransformComponent.getComponentType());
            if (tc == null) continue;

            Vector3d entityPos = tc.getPosition();
            SpikeEntry nearestSpike = ensnare.findNearestSpike(entityPos, SPIKE_HIT_RADIUS_SQ);
            if (nearestSpike == null) continue;

            applyDamage(buffer, targetRef, ensnare.getSpikeDamage(), casterRef);
            ensnare.recordDamage(targetId);
            EnsnareStyle.renderSpikeDamage(nearestSpike.getPosition(), status.getHexContext(), buffer);

            fireOnHit(status, ctx, targetRef, targetId);
        }
    }

    private void fireOnHit(HexStatus<NoState> status, ConstructTickContext ctx,
            Ref<EntityStore> targetRef, UUID targetId) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering == null) return;
        HexContext hc = status.getHexContext().branch();
        hc.updateRuntimeAccessors(ctx.getBuffer());
        triggering.writeDefaultOutput(new EntityVar(targetId, targetRef), hc);
        HexExecuter.continueExecution(triggering.getNextLinks(), hc);
    }

    private static void applyDamage(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> targetRef, float amount, Ref<EntityStore> casterRef) {
        if (damageCauseIndex < 0) {
            damageCauseIndex = DamageCause.getAssetMap().getIndex("Environment");
        }
        if (damageCauseIndex == Integer.MIN_VALUE) return;

        DamageCause cause = DamageCause.getAssetMap().getAsset(damageCauseIndex);
        if (cause == null) return;

        Damage damage = new Damage(
                new Damage.EntitySource(casterRef), cause, amount);
        DamageSystems.executeDamage(targetRef, buffer, damage);
    }

    private void removeSpikes(EnsnareComponent ensnare, HexStatus<NoState> status, CommandBuffer<EntityStore> buffer) {
        List<Ref<EntityStore>> particleRecipients = VfxUtil.collectParticleRecipients(
                ensnare.getCenter(), ensnare.getRadius() + 25.0, buffer);
        for (SpikeEntry spike : ensnare.getSpikes()) {
            Ref<EntityStore> spikeRef = spike.getEntityRef();
            if (spikeRef != null && spikeRef.isValid()) {
                EnsnareStyle.renderSpikeDespawn(
                        spike.getPosition(), status.getHexContext(), buffer, particleRecipients);
                Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
                buffer.removeEntity(spikeRef, holder, RemoveReason.REMOVE);
            }
        }
    }
}
