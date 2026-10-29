package com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale;

import java.util.Arrays;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.components.ScaleState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.handler.ScaleConstructHandler;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.style.ScaleStyle;
import com.riprod.hexcode.core.common.appearance.AppearanceLayer;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class ScaleGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Scale";

    private double minMagnitude = 0.01;
    private double maxMagnitude = 32.0;
    private double minDuration = 0.1;
    private float mountOffsetY = 2.5f;
    private String scaleEffectId;

    public static final BuilderCodec<ScaleGlyph> CODEC = BuilderCodec
            .builder(ScaleGlyph.class, ScaleGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("MinMagnitude", Codec.DOUBLE, true),
                    (c, v) -> c.minMagnitude = v, c -> c.minMagnitude)
            .add()
            .append(new KeyedCodec<>("MaxMagnitude", Codec.DOUBLE, true),
                    (c, v) -> c.maxMagnitude = v, c -> c.maxMagnitude)
            .add()
            .append(new KeyedCodec<>("MinDuration", Codec.DOUBLE, true),
                    (c, v) -> c.minDuration = v, c -> c.minDuration)
            .add()
            .append(new KeyedCodec<>("MountOffsetY", Codec.FLOAT, true),
                    (c, v) -> c.mountOffsetY = v, c -> c.mountOffsetY)
            .add()
            .append(new KeyedCodec<>("ScaleEffect", Codec.STRING, true),
                    (c, v) -> c.scaleEffectId = v, c -> c.scaleEffectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        float baseCost = glyph.computeBaseCost(asset);
        if (baseCost <= 0)
            return 0f;

        double magnitude = clamp(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.MAGNITUDE, hexContext),
                asset != null ? asset.getSlot(ScaleGlyphSlots.MAGNITUDE) : null),
                minMagnitude, maxMagnitude);

        float currentScale = readCurrentScale(glyph, hexContext);
        double resultScale = clamp(currentScale * magnitude, minMagnitude, maxMagnitude);

        var impact = asset == null
                ? null
                : asset.getVolatilityImpact();
        return baseCost * Impact.scale(impact, resultScale);
    }

    private float readCurrentScale(Glyph glyph, HexContext hexContext) {
        try {
            HexVar targets = glyph.readSlot(ScaleGlyphSlots.TARGET, hexContext);
            if (!(targets instanceof EntityVar entityVar))
                return 1.0f;
            CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
            Ref<EntityStore> targetRef = entityVar.getRef(accessor);
            if (targetRef == null || !targetRef.isValid())
                return 1.0f;
            return readRenderedScale(accessor, targetRef);
        } catch (Exception e) {
            return 1.0f;
        }
    }

    private static float readRenderedScale(CommandBuffer<EntityStore> accessor, Ref<EntityStore> targetRef) {
        EntityScaleComponent scaleComponent = accessor.getComponent(
                targetRef, EntityScaleComponent.getComponentType());
        return scaleComponent != null ? scaleComponent.getScale() : 1.0f;
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        var targets = glyph.readSlot(ScaleGlyphSlots.TARGET, hexContext);
        if (!(targets instanceof EntityVar entityVar)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be a creature");
            return new NumberVar(1);
        }

        var accessor = hexContext.getAccessor();
        var targetRef = entityVar.getRef(accessor);

        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return new NumberVar(1);
        }

        return new NumberVar(readRenderedScale(accessor, targetRef));
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(ScaleGlyphSlots.TARGET, hexContext);
        if (!(targets instanceof EntityVar entityVar)) {

            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be a creature");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> targetRef = entityVar.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return;
        }

        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (!HexProtection.canAffectEntity(accessor.getExternalData().getWorld(), caster, accessor, targetRef)) {
            HexProtection.notifyBlocked(caster, accessor, TYPE);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        double magnitude = clamp(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.MAGNITUDE, hexContext), getSlot(ScaleGlyphSlots.MAGNITUDE)),
                minMagnitude, maxMagnitude);

        float durationSeconds = (float) Math.max(minDuration, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.DURATION, hexContext), getSlot(ScaleGlyphSlots.DURATION)));

        ScaleState state = null;
        var layerApplied = false;
        var spawned = false;
        try {
            float currentScale = readRenderedScale(accessor, targetRef);
            double desiredScale = clamp(currentScale * magnitude,
                    minMagnitude, maxMagnitude);
            float contribution = currentScale > 0
                    ? (float) (desiredScale / currentScale)
                    : (float) magnitude;

            state = new ScaleState();
            state.setAppliedMagnitude((float) magnitude);
            state.setRemainingSeconds(durationSeconds);
            state.setNextGlyphIds(glyph.getNextLinks());

            layerApplied = HexAppearanceService.addLayer(accessor, targetRef,
                    state.getConstructId().toString(), AppearanceLayer.ofScale(contribution));
            if (!layerApplied) {
                HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "Cannot resolve target model");
                return;
            }

            if (VfxUtil.applyBoundedEffect(hexContext, targetRef, glyph, scaleEffectId, durationSeconds,
                    OverlapBehavior.OVERWRITE)) {
                state.setEffectId(scaleEffectId);
            }

            Vector3d spawnPos;
            TransformComponent targetTransform = accessor.getComponent(
                    targetRef, TransformComponent.getComponentType());
            if (targetTransform != null) {
                spawnPos = new Vector3d(targetTransform.getPosition());
            } else {
                spawnPos = new Vector3d();
            }

            Ref<EntityStore> visualRef = magnitude >= 1.0
                    ? spawnVisual(accessor, spawnPos, targetRef, hexContext)
                    : null;
            state.setVisualRef(visualRef);

            spawned = HexConstructSpawner.applyWithState(
                    accessor, targetRef, hexContext, glyph, ScaleGlyph.TYPE, state) != null;
            if (!spawned) {
                undo(accessor, targetRef, state);
                return;
            }

            Slot immediate = glyph.getSlot(ScaleGlyphSlots.IMMEDIATE);
            if (immediate != null && immediate.getLinks().length > 0) {
                HexContext immediateCtx = hexContext.branch();
                immediateCtx.setDefaultVariable(entityVar);
                HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
            }

            ScaleStyle.renderApply(this, spawnPos, hexContext, accessor);
        } catch (Exception e) {
            if (spawned) {
                LOGGER.atWarning().withCause(e).log("scale: post-spawn step failed");
                return;
            }
            if (layerApplied) {
                undo(accessor, targetRef, state);
            }
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot apply scale", e);
        }
    }

    private static void undo(CommandBuffer<EntityStore> accessor, Ref<EntityStore> targetRef, ScaleState state) {
        if (!targetRef.isValid()) return;
        HexAppearanceService.removeLayer(accessor, targetRef, state.getConstructId().toString());
        ScaleConstructHandler.releaseEffect(accessor, targetRef, state.getEffectId(), null);
        var visualRef = state.getVisualRef();
        if (visualRef != null && visualRef.isValid()) {
            accessor.tryRemoveEntity(visualRef, RemoveReason.REMOVE);
        }
    }

    private Ref<EntityStore> spawnVisual(CommandBuffer<EntityStore> accessor,
            Vector3d spawnPos, Ref<EntityStore> targetRef, HexContext hexContext) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(HexcodeComponent.getComponentType(), new HexcodeComponent());
        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(spawnPos, new Rotation3f()));
        holder.ensureComponent(UUIDComponent.getComponentType());
        holder.addComponent(NetworkId.getComponentType(),
                new NetworkId(accessor.getExternalData().takeNextNetworkId()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
        holder.addComponent(MountedComponent.getComponentType(),
                new MountedComponent(targetRef,
                        new Vector3f(0f, mountOffsetY, 0f),
                        MountController.Minecart));

        HexConstructSpawner.attachModel(holder, hexContext, this, 1.0f);

        return accessor.addEntity(holder, AddReason.SPAWN);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
