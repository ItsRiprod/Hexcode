package com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.system;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.utils.BlockAccess;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.state.NoState;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.ConjureGlyphSlots;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.style.ConjureStyle;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.ConjureConfig;

public class ConjureConstructHandler implements ConstructHandler<NoState> {

    private ConjureConfig resolveConfig(HexStatus<NoState> status) {
        Glyph glyph = status.getTriggeringGlyph();
        GlyphAsset asset = glyph != null ? GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId()) : null;
        return asset != null && asset.getConfig() instanceof ConjureConfig config
                ? config
                : ConjureConfig.DEFAULTS;
    }

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
        hexContext.updateRuntimeAccessors(ctx.getBuffer());
        UUID entityId = ctx.getBuffer().getComponent(ctx.getEntityRef(), UUIDComponent.getComponentType())
                .getUuid();

        HexContext immediateCtx = hexContext.branch();
        immediateCtx.setDefaultVariable(new EntityVar(entityId, ctx.getEntityRef()));
        HexExecuter.continueExecution(Arrays.asList(links), immediateCtx);
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

        Rotation3f rotation = transform.getRotation();
        EntityScaleComponent scaleComponent = ctx.getChunk().getComponent(
                ctx.getIndex(), EntityScaleComponent.getComponentType());
        float scale = scaleComponent != null ? scaleComponent.getScale() : 1f;
        if (zone.reproject(rotation.pitch(), rotation.yaw(), rotation.roll(), scale)) {
            DebugComponent debug = ctx.getBuffer().getComponent(
                    ctx.getEntityRef(), DebugComponent.getComponentType());
            if (debug != null) {
                debug.setScale(zone.getDebugSize());
            }
        }

        Glyph triggering = status.getTriggeringGlyph();

        if (zone.getDuration() > 0) {
            zone.addToTotallapsed(dt);
            if (zone.getTotallapsed() >= zone.getDuration()) {
                return true;
            }
        }

        ConjureConfig config = resolveConfig(status);
        boolean barrier = ctx.getChunk().getComponent(
                ctx.getIndex(), HitboxCollision.getComponentType()) != null;

        List<String> nextLinks = triggering.getNextLinks();
        boolean hasChildren = nextLinks != null && !nextLinks.isEmpty();

        zone.setSpatialQueryTimer(zone.getSpatialQueryTimer() - dt);
        boolean queryDue = zone.getSpatialQueryTimer() <= 0f;
        if (queryDue) {
            zone.setSpatialQueryTimer(config.getSpatialQueryInterval());
        }

        if (hasChildren && zone.getInterval() > 0) {
            zone.setIntervalTimer(zone.getIntervalTimer() - dt);
        }

        if (barrier || (hasChildren && queryDue)) {
            Vector3d pos = transform.getPosition();
            Vector3d half = zone.getAabbHalfExtents();
            double margin = config.getDefaultEntityHalfExtent();
            Vector3d min = new Vector3d(
                    pos.x - half.x - margin, pos.y - half.y - margin, pos.z - half.z - margin);
            Vector3d max = new Vector3d(
                    pos.x + half.x + margin, pos.y + half.y + margin, pos.z + half.z + margin);

            List<Ref<EntityStore>> found = new ObjectArrayList<>(TargetUtil.getAllEntitiesInBox(min, max, ctx.getBuffer()));

            if (barrier) {
                containEntities(zone, pos, found, config, ctx);
            }

            if (hasChildren && queryDue) {
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

                    TransformComponent candidate = ctx.getBuffer().getComponent(
                            ref, TransformComponent.getComponentType());
                    if (candidate == null || !zone.containsPoint(pos, candidate.getPosition()))
                        continue;

                    UUID entityId = uuid.getUuid();
                    currentOccupants.add(entityId);

                    if (!previousOccupants.contains(entityId)) {
                        fireOnEntity(status, ctx, ref, uuid, candidate);
                    }
                }

                if (zone.getInterval() > 0 && zone.getIntervalTimer() <= 0) {
                    zone.setIntervalTimer(zone.getInterval());
                    for (Ref<EntityStore> ref : found) {
                        if (ref == null || !ref.isValid())
                            continue;
                        UUIDComponent uuid = ctx.getBuffer().getComponent(ref, UUIDComponent.getComponentType());
                        if (uuid == null)
                            continue;
                        if (!currentOccupants.contains(uuid.getUuid()))
                            continue;
                        TransformComponent candidate = ctx.getBuffer().getComponent(
                                ref, TransformComponent.getComponentType());
                        fireOnEntity(status, ctx, ref, uuid, candidate);
                    }
                }
            }
        }

        return !drainSustain(dt, status);
    }

    private void containEntities(ConjureZoneComponent zone, Vector3d center,
            List<Ref<EntityStore>> found, ConjureConfig config, ConstructTickContext ctx) {
        World world = ctx.getBuffer().getExternalData().getWorld();
        double fallback = config.getDefaultEntityHalfExtent();
        Vector3d halfExtents = new Vector3d();
        Vector3d boxCenter = new Vector3d();
        Vector3d direction = new Vector3d();

        for (Ref<EntityStore> ref : found) {
            if (ref == null || !ref.isValid())
                continue;
            if (ctx.getBuffer().getComponent(ref, Player.getComponentType()) != null)
                continue;
            if (ctx.getBuffer().getComponent(ref, Intangible.getComponentType()) != null)
                continue;
            if (ctx.getBuffer().getComponent(ref, HexcodeComponent.getComponentType()) != null)
                continue;
            if (ctx.getBuffer().getComponent(ref, DeathComponent.getComponentType()) != null)
                continue;

            TransformComponent candidate = ctx.getBuffer().getComponent(
                    ref, TransformComponent.getComponentType());
            if (candidate == null)
                continue;

            Vector3d position = candidate.getPosition();
            BoundingBox boundingBox = ctx.getBuffer().getComponent(ref, BoundingBox.getComponentType());
            Box box = boundingBox != null ? boundingBox.getBoundingBox() : null;
            if (box != null) {
                halfExtents.set(box.width() / 2, box.height() / 2, box.depth() / 2);
                boxCenter.set(
                        position.x + (box.min.x + box.max.x) / 2,
                        position.y + (box.min.y + box.max.y) / 2,
                        position.z + (box.min.z + box.max.z) / 2);
            } else {
                halfExtents.set(fallback, fallback, fallback);
                boxCenter.set(position);
            }

            for (int rank = 0; rank < 3; rank++) {
                double depth = zone.computeEjection(center, boxCenter, halfExtents, rank, direction);
                if (depth <= 0) {
                    break;
                }
                double step = Math.min(depth + config.getCorrectionEpsilon(),
                        config.getMaxCorrectionPerTick());
                double x = position.x + direction.x * step;
                double y = position.y + direction.y * step;
                double z = position.z + direction.z * step;
                if (isSolid(world, x, y, z)) {
                    continue;
                }
                position.set(x, y, z);
                break;
            }
        }
    }

    private boolean isSolid(World world, double x, double y, double z) {
        BlockType blockType = BlockAccess.blockType(world,
                (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        return blockType != null && blockType.getMaterial() == BlockMaterial.Solid;
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
            Ref<EntityStore> entityRef, UUIDComponent entityUuid,
            TransformComponent entityTransform) {
        Glyph triggering = status.getTriggeringGlyph();
        if (triggering != null) {
            HexContext hexCtx = status.getHexContext().branch();
            hexCtx.updateRuntimeAccessors(ctx.getBuffer());
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
