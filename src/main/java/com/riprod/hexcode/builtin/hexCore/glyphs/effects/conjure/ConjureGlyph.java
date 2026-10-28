package com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure;

import java.util.Map;
import java.util.UUID;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.component.ConjureZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure.style.ConjureStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

import lombok.Getter;

public class ConjureGlyph extends GlyphAsset {
        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
        public static final String TYPE = "Conjure";

        @Getter 
        private double boxHalfExtent = 0.5;
        private double minAxisSize = 1.0;
        @Getter 
        private float spatialQueryInterval = 0.2f;
        private String hardCollisionId = "Hexcode_Conjure_HardCollision";
        private String softCollisionId = "Hexcode_Conjure_SoftCollision";
        private String anchorModelId = "Conjured_Anchor";
        @Getter 
        private double maxCorrectionPerTick = 0.35;
        @Getter 
        private double correctionEpsilon = 0.02;
        private double defaultEntityHalfExtent = 0.5;

        public static final BuilderCodec<ConjureGlyph> CODEC = BuilderCodec
                        .builder(ConjureGlyph.class, ConjureGlyph::new, GlyphAsset.ABSTRACT_CODEC)
                        .append(new KeyedCodec<>("BoxHalfExtent", Codec.DOUBLE, true),
                                        (c, v) -> c.boxHalfExtent = v, c -> c.boxHalfExtent)
                        .add()
                        .append(new KeyedCodec<>("MinAxisSize", Codec.DOUBLE, true),
                                        (c, v) -> c.minAxisSize = v, c -> c.minAxisSize)
                        .add()
                        .append(new KeyedCodec<>("SpatialQueryInterval", Codec.FLOAT, true),
                                        (c, v) -> c.spatialQueryInterval = v, c -> c.spatialQueryInterval)
                        .add()
                        .append(new KeyedCodec<>("HardCollision", Codec.STRING, true),
                                        (c, v) -> c.hardCollisionId = v, c -> c.hardCollisionId)
                        .addValidatorLate(() -> HitboxCollisionConfig.VALIDATOR_CACHE.getValidator().late())
                        .add()
                        .append(new KeyedCodec<>("SoftCollision", Codec.STRING, true),
                                        (c, v) -> c.softCollisionId = v, c -> c.softCollisionId)
                        .addValidatorLate(() -> HitboxCollisionConfig.VALIDATOR_CACHE.getValidator().late())
                        .add()
                        .append(new KeyedCodec<>("AnchorModel", Codec.STRING, true),
                                        (c, v) -> c.anchorModelId = v, c -> c.anchorModelId)
                        .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
                        .add()
                        .append(new KeyedCodec<>("MaxCorrectionPerTick", Codec.DOUBLE, true),
                                        (c, v) -> c.maxCorrectionPerTick = v, c -> c.maxCorrectionPerTick)
                        .add()
                        .append(new KeyedCodec<>("CorrectionEpsilon", Codec.DOUBLE, true),
                                        (c, v) -> c.correctionEpsilon = v, c -> c.correctionEpsilon)
                        .add()
                        .append(new KeyedCodec<>("DefaultEntityHalfExtent", Codec.DOUBLE, true),
                                        (c, v) -> c.defaultEntityHalfExtent = v, c -> c.defaultEntityHalfExtent)
                        .add()
                        .build();

        private static final float PASSIVE_FLOOR = 0.1f;

        private static boolean hasLinks(Glyph glyph, String slotKey) {
                Slot s = glyph.getSlot(slotKey);
                return s != null && s.getLinks().length > 0;
        }

        private boolean isPassive(Glyph glyph) {
                return glyph.getNextLinks().isEmpty() && !hasLinks(glyph, ConjureGlyphSlots.IMMEDIATE);
        }

        @Override
        public float collectMana(Glyph glyph, GlyphAsset asset) {
                if (isPassive(glyph))
                        return PASSIVE_FLOOR;
                if (asset == null)
                        return 0f;
                return asset.getManaConsumption()
                                * ((1 - glyph.getEfficiency()) * 0.25f + 0.75f);
        }

        @Override
        public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {

                Vector3d a = glyph.readSlot(ConjureGlyphSlots.COORDS_A, hexContext,
                                new PositionVar(new Vector3d(boxHalfExtent, boxHalfExtent, boxHalfExtent))).position(
                                                hexContext.getAccessor());
                Vector3d b = glyph.readSlot(ConjureGlyphSlots.COORDS_B, hexContext,
                                new PositionVar(new Vector3d(-boxHalfExtent, -boxHalfExtent, -boxHalfExtent))).position(
                                                hexContext.getAccessor());
                double volume = 1.0;
                if (a != null && b != null) {
                        double dx = Math.max(minAxisSize, Math.abs(a.x - b.x));
                        double dy = Math.max(minAxisSize, Math.abs(a.y - b.y));
                        double dz = Math.max(minAxisSize, Math.abs(a.z - b.z));
                        volume = dx * dy * dz;
                }

                float passiveImpact = 1.0f;
                if (isPassive(glyph)) {
                        passiveImpact = 0.1f;
                }

                return glyph.computeBaseCost(asset) * Impact.scale(volatilityImpact, volume) * passiveImpact;
        }

        @Override
        public void execute(Glyph glyph, HexContext hexContext) {
                GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());

                HexVar coordsAVar = glyph.readSlot(ConjureGlyphSlots.COORDS_A, hexContext,
                                new PositionVar(new Vector3d(boxHalfExtent, boxHalfExtent, boxHalfExtent)));
                HexVar coordsBVar = glyph.readSlot(ConjureGlyphSlots.COORDS_B, hexContext,
                                new PositionVar(new Vector3d(-boxHalfExtent, -boxHalfExtent, -boxHalfExtent)));
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
                Vector3d anchorPos = anchorVar.position(hexContext.getAccessor());
                if (anchorPos == null) {
                        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                                        "Anchor variable is not a valid position");
                        return;
                }

                Vector3d coordsA = coordsAVar.position(hexContext.getAccessor());
                Vector3d coordsB = coordsBVar.position(hexContext.getAccessor());

                if (coordsA == null || coordsB == null) {
                        HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                                        "Corner coordinates must be valid positions");
                        return;
                }

                var cornerA = new Vector3d(anchorPos).add(coordsA);
                var cornerB = new Vector3d(anchorPos).add(coordsB);

                var min = new Vector3d(
                                Math.min(cornerA.x, cornerB.x),
                                Math.min(cornerA.y, cornerB.y),
                                Math.min(cornerA.z, cornerB.z));
                var max = new Vector3d(
                                Math.max(cornerA.x, cornerB.x),
                                Math.max(cornerA.y, cornerB.y),
                                Math.max(cornerA.z, cornerB.z));
                var center = new Vector3d(
                                (min.x + max.x) / 2,
                                (min.y + max.y) / 2,
                                (min.z + max.z) / 2);
                var halfExtents = new Vector3d(
                                (max.x - min.x) / 2,
                                (max.y - min.y) / 2,
                                (max.z - min.z) / 2);

                float durationSeconds = HexVarUtil.numberOrSlotDefault(
                                durationVar, asset.getSlot(ConjureGlyphSlots.DURATION)).floatValue();
                float interval = HexVarUtil.numberOrSlotDefault(
                                intervalVar, asset.getSlot(ConjureGlyphSlots.INTERVAL)).floatValue();

                var zoneComp = new ConjureZoneComponent(halfExtents, interval, durationSeconds);

                double hitboxMode = HexVarUtil.numberOrSlotDefault(
                                glyph.readSlot(ConjureGlyphSlots.HITBOX, hexContext),
                                asset.getSlot(ConjureGlyphSlots.HITBOX)).doubleValue();
                var collisionId = hitboxMode > 0 ? hardCollisionId
                                : hitboxMode < 0 ? softCollisionId
                                                : null;
                var collisionConfig = collisionId != null
                                ? HitboxCollisionConfig.getAssetMap().getAsset(collisionId)
                                : null;

                var holder = HexConstructSpawner.create(
                                hexContext.getAccessor(), hexContext, glyph, ConjureGlyph.TYPE, new Vector3d(center));

                holder.ensureComponent(PropComponent.getComponentType());
                holder.ensureComponent(EffectControllerComponent.getComponentType());
                float debugAlpha = VfxUtil.resolveAlpha(hexContext, this);
                if (debugAlpha > 0f) {
                        Vector3f debugColor = VfxUtil.resolvePrimaryColor(hexContext, this);
                        DebugComponent debugComp = new DebugComponent(DebugShape.Cube, debugColor,
                                        zoneComp.getDebugSize(), 0.1f);
                        debugComp.setOpacity(debugAlpha * 0.5f);
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

                var modelAsset = ModelAsset.getAssetMap().getAsset(anchorModelId);
                if (modelAsset != null) {

                        Box modelBox = new Box(
                                        -halfExtents.x, -halfExtents.y, -halfExtents.z,
                                        halfExtents.x, halfExtents.y, halfExtents.z);
                        Model model = new Model(
                                        modelAsset.getId(), 1.0f, (Map<String, String>) null,
                                        modelAsset.getAttachments(null),
                                        modelBox, modelAsset.getModel(), modelAsset.getTexture(),
                                        modelAsset.getGradientSet(), modelAsset.getGradientId(),
                                        modelAsset.getEyeHeight(),
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

                var zoneRef = hexContext.getAccessor().addEntity(holder, AddReason.SPAWN);
                zoneComp.setZoneRef(zoneRef);

                ConjureStyle.renderSpawn(this, center, hexContext, hexContext.getAccessor());

                UUIDComponent zoneUuidComp = holder.getComponent(UUIDComponent.getComponentType());
                UUID zoneUuid = zoneUuidComp != null ? zoneUuidComp.getUuid() : UUID.randomUUID();
                EntityVar zoneEntityVar = new EntityVar(zoneUuid, zoneRef);
                glyph.writeSelfOutput(zoneEntityVar, hexContext);
                glyph.writeOutput(zoneEntityVar, hexContext);

                hexContext.addDependency(zoneRef);
        }
}
