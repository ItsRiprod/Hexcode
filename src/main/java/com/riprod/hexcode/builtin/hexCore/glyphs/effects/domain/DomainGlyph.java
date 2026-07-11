package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.component.DomainZoneComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain.style.DomainStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.utilities.component.DebugComponent;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class DomainGlyph implements GlyphHandler {
    @Override
public String getId() { return ID; };

public static final String ID = "Domain";
    public static final String AURA_ID = "Domain_Aura";

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final double DEFAULT_RADIUS = 5.0;
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 15.0;
    private static final double DEFAULT_DURATION = 10.0;
    private static final double DEFAULT_POWER = 1.0;

    @Override
    public ConfigBinding<DomainConfig> getConfigBinding() {
        return ConfigBinding.of(DomainConfig.class, DomainConfig.CODEC);
    }

    private static Impact slotImpact(GlyphAsset asset, String key) {
        if (asset == null) return null;
        SlotAsset slot = asset.getSlot(key);
        return slot == null ? null : slot.getImpact();
    }

    private static Impact triggerImpact(GlyphAsset asset) {
        return asset != null && asset.getConfig() instanceof DomainConfig dc ? dc.getTriggerImpact() : null;
    }

    @Override
    public float getVolatilityCost(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        double radius = HexVarUtil.numberOrDefault(
                glyph.readSlot(DomainGlyphSlots.MAGNITUDE, hexContext), DEFAULT_RADIUS);
        Impact impact = asset == null || asset.getConfig() == null
                ? null : asset.getConfig().getVolatilityImpact();
        return glyph.computeBaseCost(asset) * Impact.scale(impact, radius);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> casterRef = hexContext.getCasterRef(hexContext.getAccessor());
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

        Vector3d anchorPos = HexVarUtil.position(targetVar, hexContext.getAccessor());
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

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        float upfrontCost = Impact.scale(slotImpact(asset, DomainGlyphSlots.POWER), power);
        if (!hexContext.getHexRoot().tryConsumeMana(upfrontCost, hexContext.getAccessor())) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.INSUFFICIENT_MANA);
            return;
        }

        float baseDrainPerSecond = Impact.scale(slotImpact(asset, DomainGlyphSlots.MAGNITUDE), radius);
        float triggerCost = Impact.scale(triggerImpact(asset), 0);

        UUIDComponent casterUuidComp = hexContext.getAccessor().getComponent(
                casterRef, UUIDComponent.getComponentType());
        UUID casterUuid = casterUuidComp != null ? casterUuidComp.getUuid() : UUID.randomUUID();

        EntityVar zoneEntityVar = new EntityVar(UUID.randomUUID(), null);
        glyph.writeSelfOutput(zoneEntityVar, hexContext);

        Holder<EntityStore> holder = HexConstructSpawner.create(
                hexContext.getAccessor(), hexContext, glyph, DomainGlyph.ID, new Vector3d(anchorPos));

        DomainZoneComponent zoneComp = new DomainZoneComponent(
                (float) radius, durationSeconds, baseDrainPerSecond, triggerCost, power, casterUuid, casterRef);

        holder.ensureComponent(PropComponent.getComponentType());
        holder.ensureComponent(ProjectileModule.get().getProjectileComponentType());
        holder.ensureComponent(EffectControllerComponent.getComponentType());

        if (hexContext.getColors().getPrimaryAlpha() != 0f) {
            Vector3f debugColor = VfxUtil.resolvePrimaryColor(hexContext, GlyphAsset.getAssetMap().getAsset(ID));
            Vector3d debugScale = new Vector3d(radius * 2, radius * 2, radius * 2);
            DebugComponent debugComp = new DebugComponent(DebugShape.Sphere, debugColor, debugScale, 0.1f);
            debugComp.setOpacity(0.15f);
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

        DomainStyle.renderSpawn(anchorPos, (float) radius, hexContext, hexContext.getAccessor());

        hexContext.getHexRoot().addDependency(hexContext, zoneRef);

        Slot immediate = glyph.getSlot(DomainGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            immediateCtx.setDefaultVariable(zoneEntityVar);
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
