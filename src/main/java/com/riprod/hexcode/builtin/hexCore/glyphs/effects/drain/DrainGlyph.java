package com.riprod.hexcode.builtin.hexCore.glyphs.effects.drain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexColors;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.utils.HexDirectionUtil;
import com.riprod.hexcode.utils.HexVarUtil;

public class DrainGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Override
public String getId() { return ID; };

public static final String ID = "Drain";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(DrainConfig.class, DrainConfig.CODEC);
    }

    private static float conversionRate(int sourceStat, DrainConfig config) {
        if (sourceStat == DefaultEntityStatTypes.getHealth()) return config.getHpToManaRate();
        if (sourceStat == DefaultEntityStatTypes.getStamina()) return config.getStaminaToManaRate();
        return config.getDefaultConversionRate();
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        DrainConfig config = getConfig(DrainConfig.class, asset);
        if (config == null) config = DrainConfig.DEFAULTS;

        HexVar targetVar = glyph.readSlot(DrainGlyphSlots.TARGET, hexContext);
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targetVar, hexContext);
        if (entityVar == null) {
            HexExecuter.fail(glyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        var accessor = hexContext.getAccessor();

        Ref<EntityStore> targetRef = entityVar.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(glyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target entity not found");
            return;
        }

        HexVar hpInput = glyph.readSlot(DrainGlyphSlots.HP, hexContext);
        HexVar staminaInput = glyph.readSlot(DrainGlyphSlots.STAMINA, hexContext);

        int sourceStatIndex;
        double drainPercent;

        if (hpInput != null) {
            sourceStatIndex = DefaultEntityStatTypes.getHealth();
            drainPercent = HexVarUtil.numberOrDefault(hpInput, 0.0);
        } else if (staminaInput != null) {
            sourceStatIndex = DefaultEntityStatTypes.getStamina();
            drainPercent = HexVarUtil.numberOrDefault(staminaInput, 0.0);
        } else {
            sourceStatIndex = DefaultEntityStatTypes.getHealth();
            drainPercent = config.getDefaultDrainPercent();
        }

        if (sourceStatIndex == DefaultEntityStatTypes.getHealth()) {
            UUIDComponent srcUuid = hexContext.getAccessor().getComponent(
                    targetRef, UUIDComponent.getComponentType());
            UUIDComponent casterUuid = hexContext.getAccessor().getComponent(
                    hexContext.getCasterRef(accessor), UUIDComponent.getComponentType());
            if (srcUuid == null || casterUuid == null
                    || !srcUuid.getUuid().equals(casterUuid.getUuid())) {
                HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
                return;
            }
        }

        HexVar destVar = glyph.readSlot(DrainGlyphSlots.DESTINATION, hexContext);
        Ref<EntityStore> destRef = hexContext.getCasterRef(accessor);
        EntityVar destEntityVar = HexVarUtil.resolveEntityVar(destVar, hexContext);
        if (destEntityVar != null) {
            Ref<EntityStore> resolved = destEntityVar.getRef(hexContext.getAccessor());
            if (resolved != null && resolved.isValid()) destRef = resolved;
        }

        float rate = conversionRate(sourceStatIndex, config);

        if (drainPercent <= 0) {
            HexExecuter.fail(glyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Drain Percent must be greater than 0");
            return;
        }

        EntityStatMap statMap = hexContext.getAccessor().getComponent(targetRef, EntityStatMap.getComponentType());
        if (statMap == null) {
            HexExecuter.fail(glyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target has no stats");
            return;
        }

        EntityStatValue sourceStat = statMap.get(sourceStatIndex);
        if (sourceStat == null) {
            HexExecuter.fail(glyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Source stat not found on target");
            return;
        }

        float totalDrainAmount = (float) (drainPercent / 100.0) * sourceStat.getMax();

        if (sourceStatIndex == DefaultEntityStatTypes.getHealth()) {
            float maxDrainable = sourceStat.get() - config.getHpFloor();
            if (maxDrainable <= 0) {
                HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
                return;
            }
            totalDrainAmount = Math.min(totalDrainAmount, maxDrainable);
        } else {
            totalDrainAmount = Math.min(totalDrainAmount, sourceStat.get());
        }

        if (totalDrainAmount <= 0) {
            HexExecuter.fail(glyph, hexContext,
                    GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Calculated drain amount is zero or negative");
            return;
        }

        HexVar durationVar = glyph.readSlot(DrainGlyphSlots.DURATION, hexContext);
        float duration = HexVarUtil.numberOrSlotDefault(null, asset.getSlot(DrainGlyphSlots.DURATION)).floatValue();
        if (durationVar != null) {
            duration = Math.max(config.getDurationFloor(),
                    HexVarUtil.numberOrSlotDefault(durationVar, asset.getSlot(DrainGlyphSlots.DURATION)).floatValue());
        }

        HexColors colors = hexContext.getColors();
        Slot nextSlot = glyph.getSlot(Glyph.NEXT_SLOT);
        List<String> nextGlyphIds = nextSlot != null
                ? new ArrayList<>(Arrays.asList(nextSlot.getLinks()))
                : new ArrayList<>();

        DrainState state = new DrainState(
                sourceStatIndex, destRef, rate, totalDrainAmount, duration, nextGlyphIds, colors,
                config.getHpFloor());

        HexConstructSpawner.applyWithState(
                hexContext.getAccessor(), targetRef, hexContext, glyph, DrainGlyph.ID, state);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(DrainGlyphSlots.TARGET, hexContext);
        if (!(targetVar instanceof EntityVar entityVar)) return new NumberVar(0);

        Ref<EntityStore> targetRef = entityVar.getRef(hexContext.getAccessor());
        if (targetRef == null || !targetRef.isValid()) return new NumberVar(0);

        EntityStatMap statMap = hexContext.getAccessor().getComponent(targetRef, EntityStatMap.getComponentType());
        if (statMap == null) return new NumberVar(0);

        HexVar hpInput = glyph.readSlot(DrainGlyphSlots.HP, hexContext);
        HexVar staminaInput = glyph.readSlot(DrainGlyphSlots.STAMINA, hexContext);

        int statIndex;
        if (hpInput != null) {
            statIndex = DefaultEntityStatTypes.getHealth();
        } else if (staminaInput != null) {
            statIndex = DefaultEntityStatTypes.getStamina();
        } else {
            statIndex = DefaultEntityStatTypes.getMana();
        }

        EntityStatValue stat = statMap.get(statIndex);
        if (stat == null || stat.getMax() == 0) return new NumberVar(0);

        double fillPercent = (stat.get() / stat.getMax()) * 100.0;
        return new NumberVar(fillPercent);
    }
}
