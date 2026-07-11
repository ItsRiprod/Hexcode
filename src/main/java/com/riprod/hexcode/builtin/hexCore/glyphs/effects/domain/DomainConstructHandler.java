package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.state.NoState;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.component.DomainZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.style.DomainStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.VfxUtil;

public class DomainConstructHandler implements ConstructHandler<NoState> {

    private static final float AMBIENT_INTERVAL = 1.0f;
    private static final float DOMAIN_VOLATILITY_BOOST = 0.67f;
    private static final float SPATIAL_INTERVAL_SECONDS = 0.2f;
    private static final Vector3f CONTESTED_COLOR = new Vector3f(0.5f, 0.5f, 0.5f);

    @Override
    public boolean onTick(float dt, HexStatus<NoState> status, ConstructTickContext ctx) {
        DomainZoneComponent zone = ctx.getChunk().getComponent(
                ctx.getIndex(), DomainZoneComponent.getComponentType());
        TransformComponent transform = ctx.getChunk().getComponent(
                ctx.getIndex(), TransformComponent.getComponentType());
        if (zone == null || transform == null)
            return true;

        if (zone.decrementSeconds(dt)) {
            return true;
        }

        HexRoot root = status.getHexContext().getHexRoot();
        if (root == null || !root.isAlive())
            return true;
        Ref<EntityStore> rootRef = root.getSourceRef(ctx.getBuffer());
        if (rootRef == null || !rootRef.isValid())
            return true;

        Vector3d center = transform.getPosition();

        zone.setSpatialQueryTimer(zone.getSpatialQueryTimer() - dt);
        if (zone.getSpatialQueryTimer() <= 0f) {
            zone.setSpatialQueryTimer(SPATIAL_INTERVAL_SECONDS);

            updateContestation(zone, center, ctx.getEntityRef(), ctx.getBuffer());

            List<Ref<EntityStore>> found = new ObjectArrayList<>(TargetUtil.getAllEntitiesInSphere(
                    center, zone.getRadius(), ctx.getBuffer()));

            Set<UUID> previousOccupants = zone.getNewOccupants();
            Set<UUID> currentOccupants = zone.getLastOccupants();
            currentOccupants.clear();
            zone.setLastOccupants(previousOccupants);
            zone.setNewOccupants(currentOccupants);

            boolean casterInside = false;

            for (Ref<EntityStore> ref : found) {
                if (ref == null || !ref.isValid())
                    continue;
                if (ctx.getBuffer().getComponent(ref, DomainZoneComponent.getComponentType()) != null)
                    continue;

                UUIDComponent uuid = ctx.getBuffer().getComponent(ref, UUIDComponent.getComponentType());
                if (uuid == null)
                    continue;

                UUID entityId = uuid.getUuid();
                currentOccupants.add(entityId);

                if (entityId.equals(zone.getCasterUuid())) {
                    casterInside = true;
                    continue;
                }

                if (!previousOccupants.contains(entityId)) {
                    if (!root.tryConsumeMana(zone.getTriggerDrainCost(), ctx.getBuffer()))
                        return true;

                    zone.incrementTriggerCount();

                    TransformComponent entityTransform = ctx.getBuffer().getComponent(
                            ref, TransformComponent.getComponentType());
                    if (entityTransform != null) {
                        DomainStyle.renderTrigger(entityTransform.getPosition(),
                                status.getHexContext(), ctx.getBuffer());
                    }

                    Glyph triggering = status.getTriggeringGlyph();
                    if (triggering != null) {
                        HexContext hexCtx = status.getHexContext().branch();
                        hexCtx.updateRuntimeAccessors(ctx.getBuffer());
                        triggering.writeDefaultOutput(
                                new EntityVar(uuid.getUuid(), ref), hexCtx);
                        HexExecuter.continueExecution(triggering.getNextLinks(), hexCtx);
                    }
                }
            }

            updateCasterAura(zone, casterInside, ctx.getEntityRef(), ctx.getBuffer(), status);
        }

        zone.setAmbientTimer(zone.getAmbientTimer() - dt);
        if (zone.getAmbientTimer() <= 0) {
            zone.setAmbientTimer(AMBIENT_INTERVAL);
            DomainStyle.renderAmbient(center, status.getHexContext(), ctx.getBuffer());
        }

        return !drainSustain(dt, status);
    }

    @Override
    public void onCleanup(HexStatus<NoState> status, ConstructTickContext ctx) {
        DomainZoneComponent zone = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), DomainZoneComponent.getComponentType());

        var accessor = ctx.getBuffer();

        if (zone != null && zone.getCasterRef(accessor) != null && zone.getCasterRef(accessor).isValid()) {
            DomainAuraState aura = ConstructStateUtil.findState(
                    ctx.getBuffer(), zone.getCasterRef(accessor), DomainGlyph.AURA_ID, DomainAuraState.class);
            if (aura != null && ctx.getEntityRef().equals(aura.getZoneRef())) {
                ConstructStateUtil.requestKillByHandlerId(
                        ctx.getBuffer(), zone.getCasterRef(accessor), DomainGlyph.AURA_ID);
            }
        }

        TransformComponent transform = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), TransformComponent.getComponentType());
        if (transform != null) {
            float radius = zone != null ? zone.getRadius() : 5.0f;
            DomainStyle.renderDespawn(transform.getPosition(), radius,
                    status.getHexContext().getColors(), ctx.getBuffer());
        }

        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }

    private void updateCasterAura(DomainZoneComponent zone, boolean casterInside,
            Ref<EntityStore> zoneEntityRef, CommandBuffer<EntityStore> buffer,
            HexStatus<NoState> status) {
        Ref<EntityStore> casterRef = zone.getCasterRef(buffer);
        if (casterRef == null || !casterRef.isValid())
            return;

        DomainAuraState existing = ConstructStateUtil.findState(
                buffer, casterRef, DomainGlyph.AURA_ID, DomainAuraState.class);
        boolean shouldHaveAura = casterInside && !zone.isContested();

        if (shouldHaveAura && existing != null) {
            if (!zoneEntityRef.equals(existing.getZoneRef())) {
                ConstructStateUtil.requestKillByHandlerId(buffer, casterRef, DomainGlyph.AURA_ID);
                HexConstructSpawner.applyWithState(
                        buffer, casterRef, status.getHexContext(), status.getTriggeringGlyph(),
                        DomainGlyph.AURA_ID, new DomainAuraState(zoneEntityRef, DOMAIN_VOLATILITY_BOOST));
            }
        } else if (!shouldHaveAura && existing != null && zoneEntityRef.equals(existing.getZoneRef())) {
            ConstructStateUtil.requestKillByHandlerId(buffer, casterRef, DomainGlyph.AURA_ID);
        }
    }

    private void updateContestation(DomainZoneComponent self, Vector3d selfCenter,
            Ref<EntityStore> selfRef, CommandBuffer<EntityStore> buffer) {
        Store<EntityStore> store = buffer.getExternalData().getWorld().getEntityStore().getStore();
        boolean wasContested = self.isContested();
        final boolean[] nowContested = { false };

        store.forEachChunk(DomainZoneComponent.getComponentType(), (otherChunk, buf) -> {
            for (int i = 0; i < otherChunk.size(); i++) {
                Ref<EntityStore> otherRef = otherChunk.getReferenceTo(i);
                if (otherRef.equals(selfRef))
                    continue;

                DomainZoneComponent other = otherChunk.getComponent(i, DomainZoneComponent.getComponentType());
                if (other == null)
                    continue;

                TransformComponent otherTransform = buf.getComponent(otherRef, TransformComponent.getComponentType());
                if (otherTransform == null)
                    continue;

                Vector3d otherPos = otherTransform.getPosition();
                double dx = selfCenter.x - otherPos.x;
                double dy = selfCenter.y - otherPos.y;
                double dz = selfCenter.z - otherPos.z;
                double combinedRadius = self.getRadius() + other.getRadius();
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < combinedRadius * combinedRadius) {
                    if (self.getPower() <= other.getPower()) {
                        nowContested[0] = true;
                        return;
                    }
                }
            }
        });

        self.setContested(nowContested[0]);

        if (nowContested[0] && !wasContested) {
            DomainStyle.renderContested(selfCenter,
                    null, buffer);

            DebugComponent debug = buffer.getComponent(selfRef, DebugComponent.getComponentType());
            if (debug != null) {
                debug.setColor(CONTESTED_COLOR);
            }
        } else if (!nowContested[0] && wasContested) {
            DebugComponent debug = buffer.getComponent(selfRef, DebugComponent.getComponentType());
            if (debug != null) {
                debug.setColor(VfxUtil.resolvePrimaryColor(null, GlyphAsset.getAssetMap().getAsset(DomainGlyph.ID)));
            }
        }
    }
}
