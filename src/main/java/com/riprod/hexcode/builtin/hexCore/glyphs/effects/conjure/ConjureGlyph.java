package com.riprod.hexcode.builtin.hexCore.glyphs.effects.conjure;

import java.util.Map;
import java.util.UUID;

import org.joml.Vector3d;
import org.joml.Vector3f;

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
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class ConjureGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Conjure";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ConjureConfig.class, ConjureConfig.CODEC);
    }

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

        ConjureConfig config = getConfig(ConjureConfig.class, asset);
        if (config == null)
            config = ConjureConfig.DEFAULTS;
        double half = config.getBoxHalfExtent();

        Vector3d a = glyph.readSlot(ConjureGlyphSlots.COORDS_A, hexContext,
                new PositionVar(new Vector3d(half, half, half))).position(
                        hexContext.getAccessor());
        Vector3d b = glyph.readSlot(ConjureGlyphSlots.COORDS_B, hexContext,
                new PositionVar(new Vector3d(-half, -half, -half))).position(
                        hexContext.getAccessor());
        double volume = 1.0;
        if (a != null && b != null) {
            double minAxis = config.getMinAxisSize();
            double dx = Math.max(minAxis, Math.abs(a.x - b.x));
            double dy = Math.max(minAxis, Math.abs(a.y - b.y));
            double dz = Math.max(minAxis, Math.abs(a.z - b.z));
            volume = dx * dy * dz;
        }

        float passiveImpact = 1.0f;
        if (isPassive(glyph)) {
            passiveImpact = 0.1f;
        }

        Impact impact = asset == null || asset.getConfig() == null
                ? null
                : asset.getConfig().getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, volume) * passiveImpact;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ConjureConfig config = getConfig(ConjureConfig.class, asset);
        if (config == null)
            config = ConjureConfig.DEFAULTS;
        double half = config.getBoxHalfExtent();

        HexVar coordsAVar = glyph.readSlot(ConjureGlyphSlots.COORDS_A, hexContext,
                new PositionVar(new Vector3d(half, half, half)));
        HexVar coordsBVar = glyph.readSlot(ConjureGlyphSlots.COORDS_B, hexContext,
                new PositionVar(new Vector3d(-half, -half, -half)));
        HexVar durationVar = glyph.readSlot(ConjureGlyphSlots.DURATION, hexContext);
        HexVar intervalVar = glyph.readSlot(ConjureGlyphSlots.INTERVAL, hexContext);
        HexVar anchorVar = glyph.readSlot(ConjureGlyphSlots.ANCHOR, hexContext);

        if (anchorVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Anchor is required");
            return;
        }
        if (!HexVarUtil.isPositionType(anchorVar)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Anchor variable is invalid");
            return;
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
        var collisionId = hitboxMode > 0 ? config.getHardCollisionId()
                : hitboxMode < 0 ? config.getSoftCollisionId()
                        : null;
        var collisionConfig = collisionId != null
                ? HitboxCollisionConfig.getAssetMap().getAsset(collisionId)
                : null;

        var holder = HexConstructSpawner.create(
                hexContext.getAccessor(), hexContext, glyph, ConjureGlyph.ID, new Vector3d(center));

        holder.ensureComponent(PropComponent.getComponentType());
        holder.ensureComponent(EffectControllerComponent.getComponentType());
        float debugAlpha = VfxUtil.resolveAlpha(hexContext, GlyphAsset.getAssetMap().getAsset(ID));
        if (debugAlpha > 0f) {
            Vector3f debugColor = VfxUtil.resolvePrimaryColor(hexContext,
                    GlyphAsset.getAssetMap().getAsset(ID));
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

        var modelAsset = ModelAsset.getAssetMap().getAsset(config.getAnchorModelId());
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

        ConjureStyle.renderSpawn(center, hexContext, hexContext.getAccessor());

        UUIDComponent zoneUuidComp = holder.getComponent(UUIDComponent.getComponentType());
        UUID zoneUuid = zoneUuidComp != null ? zoneUuidComp.getUuid() : UUID.randomUUID();
        EntityVar zoneEntityVar = new EntityVar(zoneUuid, zoneRef);
        glyph.writeSelfOutput(zoneEntityVar, hexContext);
        glyph.writeOutput(zoneEntityVar, hexContext);

        hexContext.addDependency(zoneRef);
    }
}
