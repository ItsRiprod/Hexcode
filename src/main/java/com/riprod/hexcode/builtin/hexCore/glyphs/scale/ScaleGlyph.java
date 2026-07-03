package com.riprod.hexcode.builtin.hexCore.glyphs.scale;

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
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.scale.components.ScaleStackComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.scale.components.ScaleState;
import com.riprod.hexcode.builtin.hexCore.glyphs.scale.style.ScaleStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
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
            ScaleStackComponent stack = accessor.getComponent(
                    targetRef, ScaleStackComponent.getComponentType());
            return stack != null ? stack.productOfContributions() : 1.0f;
        } catch (Exception e) {
            return 1.0f;
        }
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

        ScaleStackComponent stack = accessor.getComponent(
                targetRef, ScaleStackComponent.getComponentType());

        if (stack == null) {
            return new NumberVar(1);
        }

        return new NumberVar(stack.productOfContributions());
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

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ScaleConfig config = getConfig(ScaleConfig.class, asset);
        if (config == null) config = ScaleConfig.DEFAULTS;

        double magnitude = clamp(HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.MAGNITUDE, hexContext), asset.getSlot(ScaleGlyphSlots.MAGNITUDE)),
                config.getMinMagnitude(), config.getMaxMagnitude());

        float durationSeconds = (float) Math.max(config.getMinDuration(), HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ScaleGlyphSlots.DURATION, hexContext), asset.getSlot(ScaleGlyphSlots.DURATION)));

        try {
            ModelComponent modelComp = accessor.getComponent(
                    targetRef, ModelComponent.getComponentType());
            if (modelComp == null || modelComp.getModel() == null) {
                HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "Target has no model");
                return;
            }

            ScaleStackComponent stack = accessor.getComponent(
                    targetRef, ScaleStackComponent.getComponentType());
            if (stack == null) {
                String baseId = modelComp.getModel().getModelAssetId();
                if (baseId == null) {
                    HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                            "Cannot resolve target model asset");
                    return;
                }
                if (Math.abs(modelComp.getModel().getScale() - 1.0f) > 1e-3f) {
                    LOGGER.atWarning().log(
                            "[hexcode] Scale capturing baseAssetId=%s with non-unit current scale=%s — "
                                    + "stale state from prior cast or external remodel?",
                            baseId, modelComp.getModel().getScale());
                }
                stack = new ScaleStackComponent(baseId);
                accessor.putComponent(targetRef, ScaleStackComponent.getComponentType(), stack);
            }

            String baseAssetId = stack.getBaseAssetId();
            ModelAsset baseModelAsset = baseAssetId != null
                    ? ModelAsset.getAssetMap().getAsset(baseAssetId)
                    : null;
            if (baseModelAsset == null) {
                HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                        "Cannot resolve base model asset");
                return;
            }

            ScaleState state = new ScaleState();
            state.setAppliedMagnitude((float) magnitude);
            state.setRemainingSeconds(durationSeconds);
            state.setModelAssetId(baseAssetId);
            state.setNextGlyphIds(glyph.getNextLinks());

            stack.put(state.getConstructId(), (float) magnitude);
            accessor.putComponent(targetRef, ScaleStackComponent.getComponentType(), stack);

            float absoluteScale = (float) clamp(stack.productOfContributions(),
                    config.getMinMagnitude(), config.getMaxMagnitude());

            applyAbsoluteScale(accessor, targetRef, baseAssetId, absoluteScale);

            PlayerSkinComponent targetSkin = accessor.getComponent(
                    targetRef, PlayerSkinComponent.getComponentType());
            if (targetSkin != null) {
                targetSkin.setNetworkOutdated();
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

            ScaleStyle.renderApply(spawnPos, hexContext, accessor);
        } catch (Exception e) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot apply scale", e);
        }
    }

    public static void applyAbsoluteScale(CommandBuffer<EntityStore> buffer,
            Ref<EntityStore> targetRef, String baseAssetId, float scale) {
        float effective = Math.abs(scale - 1.0f) < 1e-4f ? 1.0f : scale;
        ModelAsset asset = baseAssetId != null
                ? ModelAsset.getAssetMap().getAsset(baseAssetId)
                : null;
        if (asset == null)
            return;

        Model scaled = Model.createScaledModel(asset, effective);
        buffer.putComponent(targetRef, ModelComponent.getComponentType(),
                new ModelComponent(scaled));
        buffer.putComponent(targetRef, EntityScaleComponent.getComponentType(),
                new EntityScaleComponent(effective));
    }

    private Ref<EntityStore> spawnVisual(CommandBuffer<EntityStore> accessor,
            Vector3d spawnPos, Ref<EntityStore> targetRef, HexContext hexContext, ScaleConfig config) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
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
