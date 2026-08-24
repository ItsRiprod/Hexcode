package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import javax.annotation.Nullable;

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
import com.hypixel.hytale.server.core.entity.UUIDComponent;
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
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.utils.HexVarUtil;

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
    public HexVar readValue(Glyph glyph, HexContext hexContext) {

        Ref<EntityStore> casterRef = hexContext.getCasterRef(hexContext.getAccessor());
        if (casterRef == null || !casterRef.isValid())
            return new NumberVar(0.0);

        CommandBuffer<EntityStore> buffer = hexContext.getAccessor();
        EntityStatMap statMap = buffer.getComponent(
                casterRef, EntityStatMap.getComponentType());
        EntityStatValue holdStat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding())
                : null;

        if (holdStat == null || holdStat.get() < 1f) {
            return new NumberVar(0.0);
        }
        return new NumberVar(1.0);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ConcentrationConfig config = getConfig(ConcentrationConfig.class, asset);
        if (config == null)
            config = ConcentrationConfig.DEFAULTS;
        Ref<EntityStore> casterRef = hexContext.getCasterRef(hexContext.getAccessor());
        if (casterRef == null || !casterRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Caster not found");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        ConcentrationState existing = ConstructStateUtil.findState(
                accessor, casterRef, ConcentrationGlyph.ID, ConcentrationState.class);
        boolean pending = HexConstructSpawner.hasPendingApply(accessor, casterRef, ConcentrationGlyph.ID);
        if (existing != null || pending) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Already concentrating");
            return;
        }

        EntityStatMap statMap = accessor.getComponent(
                casterRef, EntityStatMap.getComponentType());
        EntityStatValue holdStat = statMap != null
                ? statMap.get(HexcodeEntityStatTypes.getIsHolding())
                : null;
        if (holdStat == null || holdStat.get() < 1f) {
            HexExecuter.branchFromSlot(glyph, ConcentrationGlyphSlots.RELEASE, hexContext);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        TransformComponent casterTransform = accessor.getComponent(
                casterRef, TransformComponent.getComponentType());
        Ref<EntityStore> visualRef = null;

        if (casterTransform != null) {
            visualRef = spawnVisual(accessor, casterTransform, casterRef, hexContext);
            ConcentrationStyle.renderSpawn(casterTransform.getPosition(), hexContext, accessor);
        }

        float manaRate = readRate(glyph, asset, hexContext, ConcentrationGlyphSlots.MANA_PER_SECOND);
        float staminaRate = readRate(glyph, asset, hexContext, ConcentrationGlyphSlots.STAMINA_PER_SECOND);
        float healthRate = readRate(glyph, asset, hexContext, ConcentrationGlyphSlots.HEALTH_PER_SECOND);

        ConcentrationState state = new ConcentrationState(visualRef);
        state.setManaRate(manaRate);
        state.setStaminaRate(staminaRate);
        state.setHealthRate(healthRate);
        state.setBonusVolatilityPerSecond(
                convert(manaRate, config.getManaImpact())
                        + convert(staminaRate, config.getStaminaImpact())
                        + convert(healthRate, config.getHealthImpact()));
        HexConstructSpawner.applyWithState(
                accessor, casterRef, hexContext.branch(), glyph, ConcentrationGlyph.ID, state);

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }

    private static float readRate(Glyph glyph, GlyphAsset asset, HexContext hexContext, String slotKey) {
        double rate = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(slotKey, hexContext),
                asset != null ? asset.getSlot(slotKey) : null).doubleValue();
        return (float) Math.max(0.0, rate);
    }

    private static float convert(float rate, @Nullable Impact impact) {
        return rate <= 0f ? 0f : rate * Impact.scale(impact, rate);
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
                new MountedComponent(casterRef,
                        new Vector3f(MOUNT_OFFSET.x, MOUNT_OFFSET.y, MOUNT_OFFSET.z),
                        MountController.Minecart));

        Model model = HexConstructSpawner.attachModel(holder, hexContext,
                GlyphAsset.getAssetMap().getAsset(ID), 1.0f);
        if (model == null) {
            LOGGER.atWarning().log("concentration: primary model not found");
        }

        return accessor.addEntity(holder, AddReason.SPAWN);
    }
}
