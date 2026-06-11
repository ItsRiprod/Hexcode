package com.riprod.hexcode.builtin.glyphs.conjure.system;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.builtin.glyphs.conjure.ConjureGlyphSlots;
import com.riprod.hexcode.builtin.glyphs.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.glyphs.conjure.style.ConjureStyle;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.state.NoState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;

public class ConjureConstructHandler implements ConstructHandler<NoState> {

    private static final float SPATIAL_INTERVAL_SECONDS = 0.2f;

    @Override
    public void onFirstTick(HexStatus<NoState> status, ConstructTickContext ctx) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering == null)
            return;
        Slot immediate = triggering.getSlot(ConjureGlyphSlots.IMMEDIATE);
        if (immediate == null)
            return;
        String[] links = immediate.getLinks();
        if (links == null || links.length == 0)
            return;
        HexContext hexContext = status.getHexContext();
        hexContext.UpdateAccessor(ctx.getBuffer());
        UUID entityId = ctx.getBuffer().getComponent(ctx.getEntityRef(), UUIDComponent.getComponentType())
                .getUuid();

        hexContext.setVariable(hexContext.getDefaultSlot(), new EntityVar(entityId, ctx.getEntityRef()));
        HexExecuter.continueExecution(Arrays.asList(links), hexContext);
    }

    @Override
    public boolean onTick(float dt, HexStatus<NoState> status, ConstructTickContext ctx) {

        ConjureZoneComponent zone = ctx.getChunk().getComponent(
                ctx.getIndex(), ConjureZoneComponent.getComponentType());
        TransformComponent transform = ctx.getChunk().getComponent(
                ctx.getIndex(), TransformComponent.getComponentType());
        if (zone == null || transform == null)
            return false;

        Velocity vel = ctx.getChunk().getComponent(ctx.getIndex(), Velocity.getComponentType());
        if (vel != null) {
            Vector3d velocity = vel.getVelocity();
            if (velocity.length() > 0) {
                Vector3d pos = transform.getPosition();
                pos.set(
                        pos.x + velocity.x * dt,
                        pos.y + velocity.y * dt,
                        pos.z + velocity.z * dt);
            }
        }

        Glyph triggering = status.getTriggeringGlyph();

        if (zone.getDuration() > 0) {
            zone.addToTotallapsed(dt);
            if (zone.getTotallapsed() >= zone.getDuration()) {
                return true;
            }
        }

        if (triggering.getNextLinks() == null || triggering.getNextLinks().size() == 0) {
            return false;
        }

        zone.setSpatialQueryTimer(zone.getSpatialQueryTimer() - dt);
        if (zone.getSpatialQueryTimer() <= 0f) {
            zone.setSpatialQueryTimer(SPATIAL_INTERVAL_SECONDS);

            Vector3d pos = transform.getPosition();
            Vector3d half = zone.getHalfExtents();
            Vector3d min = new Vector3d(pos.x - half.x, pos.y - half.y, pos.z - half.z);
            Vector3d max = new Vector3d(pos.x + half.x, pos.y + half.y, pos.z + half.z);

            List<Ref<EntityStore>> found = TargetUtil.getAllEntitiesInBox(min, max, ctx.getBuffer());

            Set<UUID> previousOccupants = zone.getNewOccupants();
            Set<UUID> currentOccupants = zone.getLastOccupants();
            currentOccupants.clear();
            zone.setLastOccupants(previousOccupants);
            zone.setNewOccupants(currentOccupants);

            for (Ref<EntityStore> ref : found) {
                if (ref == null || !ref.isValid())
                    continue;
                if (ctx.getBuffer().getComponent(ref, ConjureZoneComponent.getComponentType()) != null)
                    continue;

                UUIDComponent uuid = ctx.getBuffer().getComponent(ref, UUIDComponent.getComponentType());
                if (uuid == null)
                    continue;

                UUID entityId = uuid.getUuid();
                currentOccupants.add(entityId);

                if (!previousOccupants.contains(entityId)) {
                    fireOnEntity(status, ctx, zone, ref, uuid);
                }
            }

            if (zone.getInterval() > 0) {
                zone.setIntervalTimer(zone.getIntervalTimer() - dt);
                if (zone.getIntervalTimer() <= 0) {
                    zone.setIntervalTimer(zone.getInterval());
                    for (Ref<EntityStore> ref : found) {
                        if (ref == null || !ref.isValid())
                            continue;
                        UUIDComponent uuid = ctx.getBuffer().getComponent(ref, UUIDComponent.getComponentType());
                        if (uuid == null)
                            continue;
                        if (!currentOccupants.contains(uuid.getUuid()))
                            continue;
                        fireOnEntity(status, ctx, zone, ref, uuid);
                    }
                }
            }
        }

        return !drainSustain(dt, status);
    }

    @Override
    public void onCleanup(HexStatus<NoState> status, ConstructTickContext ctx) {
        TransformComponent transform = ctx.getBuffer().getComponent(
                ctx.getEntityRef(), TransformComponent.getComponentType());
        if (transform != null) {
            ConjureStyle.renderDespawn(transform.getPosition(),
                    status.getHexContext(), ctx.getBuffer());
        }
        ctx.getBuffer().tryRemoveEntity(ctx.getEntityRef(), RemoveReason.REMOVE);
    }

    private void fireOnEntity(HexStatus<NoState> status, ConstructTickContext ctx,
            ConjureZoneComponent zone, Ref<EntityStore> entityRef, UUIDComponent entityUuid) {
        TransformComponent entityTransform = ctx.getBuffer().getComponent(
                entityRef, TransformComponent.getComponentType());

        Glyph triggering = status.getTriggeringGlyph();
        if (triggering != null) {
            HexContext hexCtx = status.getHexContext();
            if (entityTransform != null) {
                ConjureStyle.renderTrigger(entityTransform.getPosition(),
                        hexCtx, ctx.getBuffer());
            }
            triggering.writeDefaultOutput(
                    new EntityVar(entityUuid.getUuid(), entityRef), hexCtx);
            HexExecuter.continueExecution(triggering.getNextLinks(), hexCtx);
        }
    }
}
