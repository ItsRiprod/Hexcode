package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.beam;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.BlockResolution;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.TargetFilter;
import com.riprod.hexcode.utils.VfxUtil;

public class BeamGlyph extends GlyphAsset {
    public static final String TYPE = "Beam";

    private double originOffset = 1.5;

    public static final BuilderCodec<BeamGlyph> CODEC = BuilderCodec
            .builder(BeamGlyph.class, BeamGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("OriginOffset", Codec.DOUBLE, true),
                    (c, v) -> c.originOffset = v, c -> c.originOffset)
            .add()
            .build();

    private static final double LINE_THICKNESS = 0.02;
    private static final float LINE_DURATION = 0.4f;

    @Override
    public boolean isPassive(Glyph glyph) {
        return !glyph.hasLinks(Glyph.NEXT_SLOT);
    }

    @Override
    public float collectMana(Glyph glyph, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR : super.collectMana(glyph, asset);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return isPassive(glyph) ? PASSIVE_FLOOR
                : super.getVolatilityCost(glyph, hexContext, asset);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
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

        Vector3d direction = rotVar.direction(hexContext.getAccessor());
        if (direction == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Rotation variable is not valid");
            return;
        }

        double requestedRange = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(BeamGlyphSlots.RANGE, hexContext), getSlot(BeamGlyphSlots.RANGE)).doubleValue();

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

        if (!passive && posVar instanceof EntityVar sourceEntityVar) {

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

        Vector3d beamOrigin = new Vector3d(origin).add(new Vector3d(direction).mul(originOffset));

        Vector3d endPoint;
        EffectSlot hitSlot;

        if (entityHit != null && entityHitDist < blockHitDist) {
            UUIDComponent uuidComp = hexContext.getAccessor().getComponent(entityHit, UUIDComponent.getComponentType());
            if (uuidComp == null) {
                endPoint = new Vector3d(origin).add(new Vector3d(direction).mul(beamLength));
                hitSlot = null;
            } else {
                EntityVar resultVar = new EntityVar(EntityVar.createRef(uuidComp.getUuid(), entityHit));
                glyph.writeOutput(resultVar, hexContext);
                endPoint = hexContext.getAccessor().getComponent(entityHit,
                        TransformComponent.getComponentType()).getPosition();
                hitSlot = EffectSlot.Entity;
            }
        } else if (blockHitLocation != null) {
            endPoint = blockHitLocation;
            hitSlot = EffectSlot.Block;
            if (!passive)
                glyph.writeOutput(
                        new PositionVar(BlockResolution.nudgeIntoBlock(blockHitLocation, direction)),
                        hexContext);
        } else {
            endPoint = new Vector3d(origin).add(new Vector3d(direction).mul(beamLength));
            hitSlot = null;
            if (!passive)
                glyph.writeOutput(new PositionVar(endPoint), hexContext);
        }

        if (!passive)
            chargeReach(hexContext, new Vector3d(origin).sub(endPoint).length());

        var accessor = hexContext.getAccessor();
        var effects = EffectRenderer.resolve(this, hexContext);
        EffectRenderer.render(EffectSlot.Start, effects, beamOrigin, direction, 1.0f, null, accessor);
        VfxUtil.line(accessor, beamOrigin, endPoint, VfxUtil.resolvePrimaryColor(effects), LINE_THICKNESS,
                LINE_DURATION, DebugUtils.FLAG_FADE, VfxUtil.resolveAlpha(effects));
        if (hitSlot != null) {
            EffectRenderer.render(hitSlot, effects, endPoint, null, 1.0f, null, accessor);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private void chargeReach(HexContext hexContext, double reach) {
        hexContext.consumeVolatility(Impact.scale(volatilityImpact, reach));
    }
}
