package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.beam;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.beam.style.BeamStyle;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.utils.BlockResolution;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.TargetFilter;

import java.util.ArrayList;
import java.util.List;

public class BeamGlyph implements GlyphHandler {
    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Beam";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(BeamConfig.class, BeamConfig.CODEC);
    }

    private static final float PASSIVE_FLOOR = 0.1f;

    private boolean isPassive(Glyph glyph) {
        return glyph.getNextLinks().isEmpty();
    }

    @Override
    public float collectMana(Glyph glyph, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR : GlyphHandler.super.collectMana(glyph, asset);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR
                : GlyphHandler.super.getVolatilityCost(glyph, hexContext, asset);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        BeamConfig config = getConfig(BeamConfig.class, asset);
        if (config == null)
            config = BeamConfig.DEFAULTS;
        boolean passive = isPassive(glyph);

        HexVar posVar = glyph.readSlot(BeamGlyphSlots.SOURCE, hexContext);
        HexVar rotVar = glyph.readSlot(BeamGlyphSlots.ROTATION, hexContext);
        if (rotVar == null)
            rotVar = posVar;

        if (posVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Unable to find source position");
            return;
        }

        Vector3d origin = HexVarUtil.resolveEyePosition(posVar, hexContext.getAccessor());
        if (origin == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Source entity is invalid");
            return;
        }

        Vector3d direction = HexVarUtil.resolveDirection(rotVar, origin, hexContext.getAccessor());
        if (direction == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Rotation variable is not valid");
            return;
        }

        double requestedRange = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(BeamGlyphSlots.RANGE, hexContext), asset.getSlot(BeamGlyphSlots.RANGE)).doubleValue();

        int beamLength = (int) Math.abs(requestedRange);
        if (requestedRange < 0)
            direction = new Vector3d(direction).negate();

        double dlen = direction.length();
        double nx = dlen > 1e-9 ? direction.x / dlen : 0;
        double ny = dlen > 1e-9 ? direction.y / dlen : 0;
        double nz = dlen > 1e-9 ? direction.z / dlen : 0;
        float yaw = TrigMathUtil.atan2((float) -nx, (float) -nz);
        float pitch = (float) Math.asin(Math.clamp(ny, -1.0, 1.0));
        Rotation3f rotation = new Rotation3f(pitch, yaw, 0f);
        Transform transform = new Transform(new Vector3d(origin), rotation);

        Vector3d blockHitLocation = TargetUtil.getTargetLocation(transform, blockId -> blockId != 0,
                beamLength, hexContext.getAccessor());

        Ref<EntityStore> entityHit = null;
        double blockHitDist = Double.MAX_VALUE;
        double entityHitDist = Double.MAX_VALUE;

        if (blockHitLocation != null) {
            blockHitDist = new Vector3d(origin).sub(blockHitLocation).length();
        }

        if (!passive) {
            EntityVar sourceEntityVar = HexVarUtil.resolveEntityVar(posVar, hexContext);
            if (sourceEntityVar != null) {
                Ref<EntityStore> sourceRef = sourceEntityVar.getRef(hexContext.getAccessor());
                if (sourceRef != null && sourceRef.isValid()) {
                    List<Ref<EntityStore>> candidates = new ArrayList<>(
                            TargetUtil.getAllEntitiesInSphere(origin, beamLength, hexContext.getAccessor()));
                    candidates.removeIf(ref -> ref == null || ref.equals(sourceRef));
                    entityHit = TargetFilter.getSmallestTarget(origin, new Vector3d(direction).normalize(),
                            candidates, hexContext.getAccessor(), beamLength);
                    if (entityHit != null) {
                        Vector3d entityPos = hexContext.getAccessor().getComponent(entityHit,
                                TransformComponent.getComponentType()).getPosition();
                        entityHitDist = new Vector3d(origin).sub(entityPos).length();
                    }
                }
            }
        }

        Vector3d beamOrigin = new Vector3d(origin).add(new Vector3d(direction).mul(config.getOriginOffset()));

        Vector3d endPoint;
        BeamStyle.HitType hitType;

        if (entityHit != null && entityHitDist < blockHitDist) {
            UUIDComponent uuidComp = hexContext.getAccessor().getComponent(entityHit, UUIDComponent.getComponentType());
            if (uuidComp == null) {
                endPoint = new Vector3d(origin).add(new Vector3d(direction).mul(beamLength));
                hitType = BeamStyle.HitType.MISS;
            } else {
                EntityVar resultVar = new EntityVar(EntityVar.createRef(uuidComp.getUuid(), entityHit));
                glyph.writeOutput(resultVar, hexContext);
                endPoint = hexContext.getAccessor().getComponent(entityHit,
                        TransformComponent.getComponentType()).getPosition();
                hitType = BeamStyle.HitType.ENTITY;
            }
        } else if (blockHitLocation != null) {
            endPoint = blockHitLocation;
            hitType = BeamStyle.HitType.BLOCK;
            if (!passive)
                glyph.writeOutput(
                        new PositionVar(BlockResolution.nudgeIntoBlock(blockHitLocation, direction), true),
                        hexContext);
        } else {
            endPoint = new Vector3d(origin).add(new Vector3d(direction).mul(beamLength));
            hitType = BeamStyle.HitType.MISS;
            if (!passive)
                glyph.writeOutput(new PositionVar(endPoint, true), hexContext);
        }

        if (!passive)
            chargeReach(asset, hexContext, new Vector3d(origin).sub(endPoint).length());

        BeamStyle.render(beamOrigin, endPoint, new Vector3f(rotation.x, rotation.y, rotation.z), hitType, hexContext,
                hexContext.getAccessor());

        if (!passive)
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void chargeReach(GlyphAsset asset, HexContext hexContext, double reach) {
        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getVolatilityImpact();
        if (impact == null)
            return;
        VolatilityComponent volatility = hexContext.volatility();
        if (volatility == null)
            return;
        volatility.consume(Impact.scale(impact, reach));
    }
}
