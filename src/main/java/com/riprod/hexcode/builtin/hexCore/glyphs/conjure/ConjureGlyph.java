package com.riprod.hexcode.builtin.hexCore.glyphs.conjure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.conjure.style.ConjureStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class ConjureGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Conjure";
    private static final String HARD_COLLISION_ID = "Hexcode_Conjure_HardCollision";

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        Vector3d a = HexVarUtil.position(
                glyph.readSlot(ConjureGlyphSlots.COORDS_A, hexContext,
                        new PositionVar(new Vector3d(0.5, 0.5, 0.5))),
                hexContext.getAccessor());
        Vector3d b = HexVarUtil.position(
                glyph.readSlot(ConjureGlyphSlots.COORDS_B, hexContext,
                        new PositionVar(new Vector3d(-0.5, -0.5, -0.5))),
                hexContext.getAccessor());
        double volume = 1.0;
        if (a != null && b != null) {
            double dx = Math.max(1.0, Math.abs(a.x - b.x));
            double dy = Math.max(1.0, Math.abs(a.y - b.y));
            double dz = Math.max(1.0, Math.abs(a.z - b.z));
            volume = dx * dy * dz;
        }

        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, volume);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar coordsAVar = glyph.readSlot(ConjureGlyphSlots.COORDS_A, hexContext,
                new PositionVar(new Vector3d(0.5, 0.5, 0.5)));
        HexVar coordsBVar = glyph.readSlot(ConjureGlyphSlots.COORDS_B, hexContext,
                new PositionVar(new Vector3d(-0.5, -0.5, -0.5)));
        HexVar durationVar = glyph.readSlot(ConjureGlyphSlots.DURATION, hexContext);
        HexVar intervalVar = glyph.readSlot(ConjureGlyphSlots.INTERVAL, hexContext);
        HexVar anchorVar = glyph.readSlot(ConjureGlyphSlots.ANCHOR, hexContext);

        if (anchorVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Anchor is required");
            return;
        }
        if (anchorVar instanceof NumberVar anchorNum) {
            HexVar resolvedVar = hexContext.getVariable(anchorNum.getValue().toString());
            if (resolvedVar == null) {
                HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "Anchor variable is invalid");
                return;
            }
            anchorVar = resolvedVar;
        }
        Vector3d anchorPos = HexVarUtil.position(anchorVar, hexContext.getAccessor());
        if (anchorPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Anchor variable is not a valid position");
            return;
        }

        Vector3d coordsA = HexVarUtil.position(coordsAVar, hexContext.getAccessor());
        Vector3d coordsB = HexVarUtil.position(coordsBVar, hexContext.getAccessor());

        if (coordsA == null || coordsB == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Corner coordinates must be valid positions");
            return;
        }

        boolean absA = (coordsAVar instanceof PositionVar pa && pa.isAbsolute())
                || !(coordsAVar instanceof PositionVar);
        boolean absB = (coordsBVar instanceof PositionVar pb && pb.isAbsolute())
                || !(coordsBVar instanceof PositionVar);
        Vector3d cornerA = absA ? coordsA : new Vector3d(anchorPos).add(coordsA);
        Vector3d cornerB = absB ? coordsB : new Vector3d(anchorPos).add(coordsB);

        Vector3d min = new Vector3d(
                Math.min(cornerA.x, cornerB.x),
                Math.min(cornerA.y, cornerB.y),
                Math.min(cornerA.z, cornerB.z));
        Vector3d max = new Vector3d(
                Math.max(cornerA.x, cornerB.x),
                Math.max(cornerA.y, cornerB.y),
                Math.max(cornerA.z, cornerB.z));
        Vector3d center = new Vector3d(
                (min.x + max.x) / 2,
                (min.y + max.y) / 2,
                (min.z + max.z) / 2);
        Vector3d halfExtents = new Vector3d(
                (max.x - min.x) / 2,
                (max.y - min.y) / 2,
                (max.z - min.z) / 2);
        Vector3d size = new Vector3d(max.x - min.x, max.y - min.y, max.z - min.z);

        float durationSeconds = HexVarUtil.numberOrDefault(durationVar, 30.0).floatValue();
        float interval = HexVarUtil.numberOrDefault(intervalVar, -1.0).floatValue();

        ConjureZoneComponent zoneComp = new ConjureZoneComponent(halfExtents, interval, durationSeconds);

        HitboxCollisionConfig collisionConfig = HitboxCollisionConfig.getAssetMap()
                .getAsset(HARD_COLLISION_ID);

        Holder<EntityStore> holder = HexConstructSpawner.create(
                hexContext.getAccessor(), hexContext, glyph, ConjureGlyph.ID, new Vector3d(center));

        holder.ensureComponent(PropComponent.getComponentType());
        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.ensureComponent(EffectControllerComponent.getComponentType());
        // alpha of 0 means an invisible zone: skip the debug shape but keep the functional zone
        if (hexContext.getColors().getPrimaryAlpha() != 0f) {
            Vector3f debugColor = ConjureStyle.resolveColor(hexContext);
            DebugComponent debugComp = new DebugComponent(DebugShape.Cube, debugColor, size, 0.1f);
            debugComp.setOpacity(hexContext.getColors().getPrimaryAlpha() * 0.5f);
            debugComp.setIntervalMultiplier(0.01f);
            debugComp.setFlags(DebugUtils.FLAG_NO_WIREFRAME);
            holder.addComponent(DebugComponent.getComponentType(), debugComp);
        }
        holder.addComponent(BoundingBox.getComponentType(),
                new BoundingBox(Box.horizontallyCentered(halfExtents.x * 2, halfExtents.y * 2,
                        halfExtents.z * 2)));
        holder.addComponent(Velocity.getComponentType(), new Velocity());

        if (collisionConfig != null) {
            holder.addComponent(HitboxCollision.getComponentType(),
                    new HitboxCollision(collisionConfig));
        }

        ConjurePhysicsConfig.INSTANCE.apply(holder, hexContext.getCasterRef(hexContext.getAccessor()),
                new Vector3d(0, 0, 0), hexContext.getAccessor(), false);

        holder.addComponent(ConjureZoneComponent.getComponentType(), zoneComp);

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Conjured_Anchor");
        if (modelAsset != null) {

            Box modelBox = new Box(
                    -halfExtents.x, -halfExtents.y, -halfExtents.z,
                    halfExtents.x, halfExtents.y, halfExtents.z);
            Model model = new Model(
                    modelAsset.getId(), 1.0f, (Map<String, String>) null, modelAsset.getAttachments(null),
                    modelBox, modelAsset.getModel(), modelAsset.getTexture(),
                    modelAsset.getGradientSet(), modelAsset.getGradientId(), modelAsset.getEyeHeight(),
                    modelAsset.getCrouchOffset(), modelAsset.getSittingOffset(),
                    modelAsset.getSleepingOffset(),
                    modelAsset.getAnimationSetMap(), modelAsset.getCamera(),
                    modelAsset.getLight(), modelAsset.getParticles(), modelAsset.getTrails(),
                    modelAsset.getPhysicsValues(),
                    modelAsset.getDetailBoxes(), modelAsset.getPhobia(),
                    modelAsset.getPhobiaModelAssetId());

            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(),
                    new PersistentModel(model.toReference()));
        }

        Ref<EntityStore> zoneRef = hexContext.getAccessor().addEntity(holder, AddReason.SPAWN);
        zoneComp.setZoneRef(zoneRef);

        ConjureStyle.renderSpawn(center, hexContext, hexContext.getAccessor());

        UUIDComponent zoneUuidComp = holder.getComponent(UUIDComponent.getComponentType());
        UUID zoneUuid = zoneUuidComp != null ? zoneUuidComp.getUuid() : UUID.randomUUID();
        EntityVar zoneEntityVar = new EntityVar(zoneUuid, zoneRef);
        glyph.writeSelfOutput(zoneEntityVar, hexContext);
        glyph.writeOutput(zoneEntityVar, hexContext);

        hexContext.getHexRoot().addDependency(hexContext, zoneRef);
    }
}
