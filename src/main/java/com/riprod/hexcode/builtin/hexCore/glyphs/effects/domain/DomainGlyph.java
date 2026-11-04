package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.component.DomainZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.style.DomainStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.cast.component.ManaSourceComponent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

import lombok.Getter;

public class DomainGlyph extends GlyphAsset {
    public static final String TYPE = "Domain";

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final double DEFAULT_RADIUS = 5.0;
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 15.0;
    private static final double DEFAULT_DURATION = 10.0;
    private static final double DEFAULT_POWER = 1.0;

    @Nullable
    private Impact triggerImpact;
    @Nullable
    @Getter 
    private ModelParticle despawnParticle;
    @Getter 
    @Nullable
    private String despawnSound;
    @Getter 
    @Nullable
    private ModelParticle contestedParticle;
    @Getter 
    @Nullable
    private String contestedSound;

    public static final BuilderCodec<DomainGlyph> CODEC = BuilderCodec
            .builder(DomainGlyph.class, DomainGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("TriggerImpact", Impact.CODEC),
                    (c, v) -> c.triggerImpact = v, c -> c.triggerImpact)
            .add()
            .append(new KeyedCodec<>("DespawnParticle", ModelParticle.CODEC),
                    (c, v) -> c.despawnParticle = v, c -> c.despawnParticle)
            .add()
            .append(new KeyedCodec<>("DespawnSound", Codec.STRING),
                    (c, v) -> c.despawnSound = v, c -> c.despawnSound)
            .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("ContestedParticle", ModelParticle.CODEC),
                    (c, v) -> c.contestedParticle = v, c -> c.contestedParticle)
            .add()
            .append(new KeyedCodec<>("ContestedSound", Codec.STRING),
                    (c, v) -> c.contestedSound = v, c -> c.contestedSound)
            .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    private Impact slotImpact(String key) {
        var slot = getSlot(key);
        return slot == null ? null : slot.getImpact();
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double radius = HexVarUtil.numberOrDefault(
                glyph.readSlot(DomainGlyphSlots.MAGNITUDE, hexContext), DEFAULT_RADIUS);
        var impact = asset == null
                ? null
                : asset.getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, radius);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> casterRef = hexContext.getRootRef();
        if (casterRef == null || !casterRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Caster not found");
            return;
        }

        HexVar targetVar = glyph.readSlot(DomainGlyphSlots.TARGET, hexContext);
        if (targetVar == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        Vector3d anchorPos = targetVar.position(hexContext.getAccessor());
        if (anchorPos == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target position invalid");
            return;
        }

        double radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS,
                HexVarUtil.numberOrDefault(
                        glyph.readSlot(DomainGlyphSlots.MAGNITUDE, hexContext), DEFAULT_RADIUS)));

        float durationSeconds = HexVarUtil.numberOrDefault(
                glyph.readSlot(DomainGlyphSlots.DURATION, hexContext), DEFAULT_DURATION).floatValue();

        float power = HexVarUtil.numberOrDefault(
                glyph.readSlot(DomainGlyphSlots.POWER, hexContext), DEFAULT_POWER).floatValue();
        power = Math.max(0.1f, power);

        float upfrontCost = Impact.scale(slotImpact(DomainGlyphSlots.POWER), power);
        if (!hexContext.get(ManaSourceComponent.TYPE).tryConsume(upfrontCost, hexContext.getAccessor())) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.INSUFFICIENT_MANA);
            return;
        }

        float baseDrainPerSecond = Impact.scale(slotImpact(DomainGlyphSlots.MAGNITUDE), radius);
        float triggerCost = Impact.scale(triggerImpact, 0);

        UUIDComponent casterUuidComp = hexContext.getAccessor().getComponent(
                casterRef, UUIDComponent.getComponentType());
        UUID casterUuid = casterUuidComp != null ? casterUuidComp.getUuid() : UUID.randomUUID();

        EntityVar zoneEntityVar = new EntityVar(UUID.randomUUID(), null);
        glyph.writeSelfOutput(zoneEntityVar, hexContext);

        Holder<EntityStore> holder = HexConstructSpawner.create(
                hexContext.getAccessor(), hexContext, glyph, DomainGlyph.TYPE, new Vector3d(anchorPos));
        if (holder == null) {
            return;
        }

        DomainZoneComponent zoneComp = new DomainZoneComponent(
                (float) radius, durationSeconds, baseDrainPerSecond, triggerCost, power, casterUuid);

        holder.ensureComponent(PropComponent.getComponentType());
        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.ensureComponent(EffectControllerComponent.getComponentType());

        float debugAlpha = VfxUtil.resolveAlpha(hexContext, this);
        if (debugAlpha > 0f) {
            Vector3f debugColor = VfxUtil.resolvePrimaryColor(hexContext, this);
            Vector3d debugScale = new Vector3d(radius * 2, radius * 2, radius * 2);
            DebugComponent debugComp = new DebugComponent(DebugShape.Sphere, debugColor, debugScale, 0.1f);
            debugComp.setOpacity(debugAlpha * 0.15f);
            debugComp.setIntervalMultiplier(0.01f);
            debugComp.setFadeMultiplier(2.0f);
            debugComp.setFlags(DebugUtils.FLAG_NO_WIREFRAME);
            holder.addComponent(DebugComponent.getComponentType(), debugComp);
        }
        holder.addComponent(DomainZoneComponent.getComponentType(), zoneComp);

        Ref<EntityStore> zoneRef = hexContext.getAccessor().addEntity(holder, AddReason.SPAWN);
        zoneComp.setZoneRef(zoneRef);

        UUIDComponent zoneUuidComp = holder.getComponent(UUIDComponent.getComponentType());
        if (zoneUuidComp != null) {
            zoneEntityVar = new EntityVar(zoneUuidComp.getUuid(), zoneRef);
            glyph.writeSelfOutput(zoneEntityVar, hexContext);
        }

        DomainStyle.renderSpawn(this, anchorPos, (float) radius, hexContext, hexContext.getAccessor());

        hexContext.addDependency(zoneRef);
    }
}
