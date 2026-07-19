package com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale;

import java.util.Arrays;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.appearance.AppearanceLayer;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.components.ScaleState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.scale.style.ScaleStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.utils.HexVarUtil;

public class ScaleGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Scale";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ScaleConfig.class, ScaleConfig.CODEC);
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        float baseCost = glyph.computeBaseCost(asset);
        if (baseCost <= 0)
            return 0f;

        ScaleConfig config = getConfig(ScaleConfig.class, asset);
        if (config == null) config = ScaleConfig.DEFAULTS;

        double magnitude = clamp(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.MAGNITUDE, hexContext),
                asset != null ? asset.getSlot(ScaleGlyphSlots.MAGNITUDE) : null),
                config.getMinMagnitude(), config.getMaxMagnitude());

        float currentScale = readCurrentScale(glyph, hexContext);
        double resultScale = clamp(currentScale * magnitude, config.getMinMagnitude(), config.getMaxMagnitude());

        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getVolatilityImpact();
        return baseCost * Impact.scale(impact, resultScale);
    }

    private float readCurrentScale(Glyph glyph, HexContext hexContext) {
        try {
            HexVar targets = glyph.readSlot(ScaleGlyphSlots.TARGET, hexContext);
            EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
            if (entityVar == null)
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
        var entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
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
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
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
            HexProtection.notifyBlocked(caster, accessor, ID);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ScaleConfig config = getConfig(ScaleConfig.class, asset);
        if (config == null) config = ScaleConfig.DEFAULTS;

        double magnitude = clamp(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.MAGNITUDE, hexContext), asset.getSlot(ScaleGlyphSlots.MAGNITUDE)),
                config.getMinMagnitude(), config.getMaxMagnitude());

        float durationSeconds = (float) Math.max(config.getMinDuration(), HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.DURATION, hexContext), asset.getSlot(ScaleGlyphSlots.DURATION)));

        try {
            float currentScale = readRenderedScale(accessor, targetRef);
            double desiredScale = clamp(currentScale * magnitude,
                    config.getMinMagnitude(), config.getMaxMagnitude());
            float contribution = currentScale > 0
                    ? (float) (desiredScale / currentScale)
                    : (float) magnitude;

            ScaleState state = new ScaleState();
            state.setAppliedMagnitude((float) magnitude);
            state.setRemainingSeconds(durationSeconds);
            state.setNextGlyphIds(glyph.getNextLinks());

            boolean applied = HexAppearanceService.addLayer(accessor, targetRef,
                    state.getConstructId().toString(), AppearanceLayer.ofScale(contribution));
            if (!applied) {
                HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "Cannot resolve target model");
                return;
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
                    ? spawnVisual(accessor, spawnPos, targetRef, hexContext, config)
                    : null;
            state.setVisualRef(visualRef);

            HexConstructSpawner.applyWithState(
                    accessor, targetRef, hexContext, glyph, ScaleGlyph.ID, state);

            Slot immediate = glyph.getSlot(ScaleGlyphSlots.IMMEDIATE);
            if (immediate != null && immediate.getLinks().length > 0) {
                HexContext immediateCtx = hexContext.branch();
                immediateCtx.setDefaultVariable(entityVar);
                HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
            }

            ScaleStyle.renderApply(spawnPos, hexContext, accessor);
        } catch (Exception e) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot apply scale", e);
        }
    }

    private Ref<EntityStore> spawnVisual(CommandBuffer<EntityStore> accessor,
            Vector3d spawnPos, Ref<EntityStore> targetRef, HexContext hexContext, ScaleConfig config) {
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
                        new Rotation3f(0f, config.getMountOffsetY(), 0f),
                        MountController.Minecart));

        String modelId = VfxUtil.resolveModelId(hexContext, GlyphAsset.getAssetMap().getAsset(ID));
        ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;
        if (modelAsset != null) {
            Model model = Model.createUnitScaleModel(modelAsset);
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(),
                    new PersistentModel(model.toReference()));
        }

        return accessor.addEntity(holder, AddReason.SPAWN);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
