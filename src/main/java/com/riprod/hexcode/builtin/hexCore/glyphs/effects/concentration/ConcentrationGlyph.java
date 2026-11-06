package com.riprod.hexcode.builtin.hexCore.glyphs.effects.concentration;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.protection.HexcodeComponent;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;
import com.riprod.hexcode.utils.HexVarUtil;

import lombok.Getter;

public class ConcentrationGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final float DEFAULT_SUSTAIN_PER_SECOND = 3.0f;

    @Nullable
    @Getter
    private Impact sustainImpact;
    @Nullable
    @Getter
    private Impact manaImpact;
    @Nullable
    @Getter
    private Impact staminaImpact;
    @Nullable
    @Getter
    private Impact healthImpact;
    @Getter
    private String healthDamageCauseId = "Arcane";

    public static final BuilderCodec<ConcentrationGlyph> CODEC = BuilderCodec
            .builder(ConcentrationGlyph.class, ConcentrationGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("SustainImpact", Impact.CODEC, true),
                    (c, v) -> c.sustainImpact = v, c -> c.sustainImpact)
            .add()
            .append(new KeyedCodec<>("ManaImpact", Impact.CODEC, true),
                    (c, v) -> c.manaImpact = v, c -> c.manaImpact)
            .add()
            .append(new KeyedCodec<>("StaminaImpact", Impact.CODEC, true),
                    (c, v) -> c.staminaImpact = v, c -> c.staminaImpact)
            .add()
            .append(new KeyedCodec<>("HealthImpact", Impact.CODEC, true),
                    (c, v) -> c.healthImpact = v, c -> c.healthImpact)
            .add()
            .append(new KeyedCodec<>("HealthDamageCause", DamageCause.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.healthDamageCauseId = v, c -> c.healthDamageCauseId)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    public static final String TYPE = "Concentration";

    private static final Vector3f MOUNT_OFFSET = new Vector3f(0f, 1.4f, 1.2f);

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {

        Ref<EntityStore> casterRef = hexContext.getRootRef();
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
        Ref<EntityStore> casterRef = hexContext.getRootRef();
        if (casterRef == null || !casterRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Caster not found");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        ConcentrationState existing = ConstructStateUtil.findState(
                accessor, casterRef, ConcentrationGlyph.TYPE, ConcentrationState.class);
        boolean pending = HexConstructSpawner.hasPendingApply(accessor, casterRef, ConcentrationGlyph.TYPE);
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
            var flow = new ArrayList<>(glyph.getLinks(ConcentrationGlyphSlots.RELEASE));
            flow.addAll(glyph.getNextLinks());
            HexExecuter.continueExecution(flow, hexContext);
            return;
        }

        TransformComponent casterTransform = accessor.getComponent(
                casterRef, TransformComponent.getComponentType());
        Ref<EntityStore> visualRef = null;

        if (casterTransform != null) {
            visualRef = spawnVisual(accessor, casterTransform, casterRef, hexContext);
            EffectRenderer.render(EffectSlot.Start, this, hexContext, casterTransform.getPosition(), accessor);
        }

        float manaRate = readRate(glyph, hexContext, ConcentrationGlyphSlots.MANA_PER_SECOND);
        float staminaRate = readRate(glyph, hexContext, ConcentrationGlyphSlots.STAMINA_PER_SECOND);
        float healthRate = readRate(glyph, hexContext, ConcentrationGlyphSlots.HEALTH_PER_SECOND);

        ConcentrationState state = new ConcentrationState(visualRef);
        state.setManaRate(manaRate);
        state.setStaminaRate(staminaRate);
        state.setHealthRate(healthRate);
        state.setBonusVolatilityPerSecond(
                convert(manaRate, getManaImpact())
                        + convert(staminaRate, getStaminaImpact())
                        + convert(healthRate, getHealthImpact()));
        var holder = glyph.hasLinks(Glyph.NEXT_SLOT) ? hexContext.branch() : hexContext;
        HexConstructSpawner.applyWithState(
                accessor, casterRef, holder, glyph, ConcentrationGlyph.TYPE, state);

        if (holder != hexContext) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }

    private float readRate(Glyph glyph, HexContext hexContext, String slotKey) {
        double rate = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(slotKey, hexContext), getSlot(slotKey)).doubleValue();
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

        var model = HexConstructSpawner.resolveModel(hexContext, this);
        if (model == null) {
            LOGGER.atWarning().log("concentration: primary model not found");
        } else {
            HexConstructSpawner.applyModel(holder, hexContext,
                    EffectRenderer.inject(model, EffectRenderer.resolve(this, hexContext), null));
        }

        return accessor.addEntity(holder, AddReason.SPAWN);
    }
}
