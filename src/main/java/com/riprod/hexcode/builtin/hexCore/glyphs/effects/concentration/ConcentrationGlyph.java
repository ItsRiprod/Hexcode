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
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration.style.ConcentrationStyle;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.utils.HexVarUtil;
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

        // findState catches an already-applied HexEffectsComponent; hasPendingApply catches
        // a same-tick second cast still queued in HexConstructSpawner's pending-apply cache
        ConcentrationState existing = ConstructStateUtil.findState(
                accessor, casterRef, ConcentrationGlyph.ID, ConcentrationState.class);
        boolean pending = HexConstructSpawner.hasPendingApply(casterRef, ConcentrationGlyph.ID);
        if (existing != null || pending) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Already concentrating");
            return;
        }

        EntityStatMap statMap = accessor.getComponent(
                casterRef, EntityStatMap.getComponentType());
        EntityStatValue holdStat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding()) : null;
        if (holdStat == null || holdStat.get() < 1f) {
            HexExecuter.continueFromSlot(glyph, ConcentrationGlyphSlots.RELEASE, hexContext);
            return;
        }

        TransformComponent casterTransform = accessor.getComponent(
                casterRef, TransformComponent.getComponentType());
        Ref<EntityStore> visualRef = null;

        if (casterTransform != null) {
            visualRef = spawnVisual(accessor, casterTransform, casterRef, hexContext);
            ConcentrationStyle.renderSpawn(casterTransform.getPosition(), hexContext, accessor);
        }

        int resource = (int) HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(ConcentrationGlyphSlots.RESOURCE, hexContext),
                asset != null ? asset.getSlot(ConcentrationGlyphSlots.RESOURCE) : null).doubleValue();

        ConcentrationState state = new ConcentrationState(visualRef);
        state.setUpkeepActive(true);
        state.setResource(resource);
        HexConstructSpawner.applyWithState(
                accessor, casterRef, hexContext, glyph, ConcentrationGlyph.ID, state);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private Ref<EntityStore> spawnVisual(CommandBuffer<EntityStore> accessor,
            TransformComponent casterTransform, Ref<EntityStore> casterRef, HexContext hexContext) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(HexcodeComponent.getComponentType(), new HexcodeComponent());
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
