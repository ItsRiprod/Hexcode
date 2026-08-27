package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare;

import java.util.List;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.state.NoState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component.EnsnareComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.component.SpikeEntry;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare.style.EnsnareStyle;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class EnsnareConstructHandler implements ConstructHandler<NoState> {

    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);
    private static int damageCauseIndex = -1;

    @Override
    public boolean onTick(float dt, HexStatus<NoState> status, ConstructTickContext ctx) {
        EnsnareComponent ensnare = ctx.getChunk().getComponent(
                ctx.getIndex(), EnsnareComponent.getComponentType());
        if (ensnare == null) return true;

        ensnare.incrementElapsed(dt);
        if (ensnare.getElapsedSeconds() >= ensnare.getDurationSeconds()) return true;

        processDamage(ensnare, status, ctx, status.getHexContext().getCasterRef(ctx.getBuffer()));
        return !drainSustain(dt, status);
    }

    private static EnsnareConfig resolveConfig(Glyph triggeringGlyph) {
        if (triggeringGlyph == null) return EnsnareConfig.DEFAULTS;
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(triggeringGlyph.getGlyphId());
        if (asset == null) return EnsnareConfig.DEFAULTS;
        return asset.getConfig() instanceof EnsnareConfig ec ? ec : EnsnareConfig.DEFAULTS;
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
        EnsnareConfig config = resolveConfig(status.getTriggeringGlyph());

        CommandBuffer<EntityStore> buffer = ctx.getBuffer();
        Vector3d center = ensnare.getCenter();
        double radius = ensnare.getRadius() + config.getBoxPaddingXZ();
        Vector3d min = new Vector3d(center.x - radius, center.y - config.getBoxPaddingYMin(), center.z - radius);
        Vector3d max = new Vector3d(center.x + radius, center.y + config.getBoxPaddingYMax(), center.z + radius);

        List<Ref<EntityStore>> nearbyEntities = new ObjectArrayList<>(TargetUtil.getAllEntitiesInBox(min, max, buffer));
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
            SpikeEntry nearestSpike = ensnare.findNearestSpike(entityPos, config.getSpikeHitRadiusSq());
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
                holder.addComponent(HexcodeComponent.getComponentType(), new HexcodeComponent());
                buffer.removeEntity(spikeRef, holder, RemoveReason.REMOVE);
            }
        }
    }
}
