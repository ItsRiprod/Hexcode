package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.style.ConcentrationStyle;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.utils.VfxUtil;

public class ConcentrationGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    }

    public static final String ID = "Concentration";

    private static final Vector3f MOUNT_OFFSET = new Vector3f(0f, 1.4f, 1.2f);

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ConcentrationConfig.class, ConcentrationConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ConcentrationConfig config = getConfig(ConcentrationConfig.class, asset);
        if (config == null) config = ConcentrationConfig.DEFAULTS;
        Ref<EntityStore> casterRef = hexContext.getCasterRef(hexContext.getAccessor());
        if (casterRef == null || !casterRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Caster not found");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        CasterStateComponent execComp = accessor.getComponent(
                casterRef, CasterStateComponent.getComponentType());
        if (execComp == null || !execComp.isHoldingPrimary()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Caster not holding primary");
            return;
        }

        TransformComponent casterTransform = accessor.getComponent(
                casterRef, TransformComponent.getComponentType());
        Ref<EntityStore> visualRef = null;

        if (casterTransform != null) {
            visualRef = spawnVisual(accessor, casterTransform, casterRef, hexContext);
            ConcentrationStyle.renderSpawn(casterTransform.getPosition(), hexContext, accessor);
        }

        var tracker = hexContext.getHexStats();
        float bonus = tracker != null
                ? tracker.getInitialVolatility() * (float) config.getVolatilityBonusFraction() : 0f;
        if (tracker != null && bonus > 0f) {
            tracker.addVolatility(bonus);
        }

        ConcentrationState state = new ConcentrationState(visualRef);
        state.setVolatilityBonus(bonus);
        HexConstructSpawner.applyWithState(
                accessor, casterRef, hexContext, glyph, ConcentrationGlyph.ID, state);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private Ref<EntityStore> spawnVisual(CommandBuffer<EntityStore> accessor,
            TransformComponent casterTransform, Ref<EntityStore> casterRef, HexContext hexContext) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(TransformComponent.getComponentType(),
                new TransformComponent(casterTransform.getPosition(), new Rotation3f()));
        holder.ensureComponent(UUIDComponent.getComponentType());
        holder.addComponent(NetworkId.getComponentType(),
                new NetworkId(accessor.getExternalData().takeNextNetworkId()));
        holder.ensureComponent(EntityStore.REGISTRY.getNonSerializedComponentType());
        holder.addComponent(MountedComponent.getComponentType(),
                new MountedComponent(casterRef, new Rotation3f(MOUNT_OFFSET.x, MOUNT_OFFSET.y, MOUNT_OFFSET.z), MountController.Minecart));

        String modelId = VfxUtil.resolveModelId(hexContext, GlyphAsset.getAssetMap().getAsset(ID));
        ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;
        if (modelAsset != null) {
            Model model = Model.createUnitScaleModel(modelAsset);
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(),
                    new PersistentModel(model.toReference()));
        } else {
            LOGGER.atWarning().log("concentration: primary model '%s' not found", modelId);
        }

        return accessor.addEntity(holder, AddReason.SPAWN);
    }
}
