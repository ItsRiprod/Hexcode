package com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility.style.InvisibilityStyle;
import com.riprod.hexcode.core.common.appearance.AppearanceLayer;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.utils.HexVarUtil;

import java.util.Arrays;

public class InvisibilityGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() { return ID; }

    public static final String ID = "Invisibility";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(InvisibilityConfig.class, InvisibilityConfig.CODEC);
    }

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        EntityVar entityVar = HexVarUtil.resolveEntityVar(
                glyph.readSlot(InvisibilityGlyphSlots.TARGET, hexContext), hexContext);
        Ref<EntityStore> ref = entityVar != null ? entityVar.getRef(accessor) : null;
        if (ref == null || !ref.isValid()) return new NumberVar(0);
        InvisibilityState state = ConstructStateUtil.findState(
                accessor, ref, InvisibilityGlyph.ID, InvisibilityState.class);
        return new NumberVar(state != null ? state.getRemainingDuration() : 0);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(InvisibilityGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        InvisibilityConfig config = getConfig(InvisibilityConfig.class, asset);
        if (config == null) config = InvisibilityConfig.DEFAULTS;

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(InvisibilityGlyphSlots.DURATION, hexContext),
                asset.getSlot(InvisibilityGlyphSlots.DURATION));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        boolean sustained = entityVar != null
                && applyToEntities(glyph, entityVar, (float) duration, config, hexContext, accessor);

        // sustained cast defers continuation to the construct's onEnd; otherwise pass through
        if (sustained) {
            Slot immediate = glyph.getSlot(InvisibilityGlyphSlots.IMMEDIATE);
            if (immediate != null && immediate.getLinks().length > 0) {
                HexContext immediateCtx = hexContext.branch();
                immediateCtx.setDefaultVariable(entityVar);
                HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
            }
        } else {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }

    private boolean applyToEntities(Glyph glyph, EntityVar entityVar, float durationSeconds,
            InvisibilityConfig config, HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) return false;

        String effectId = config.getEffectId();
        EntityEffect invisibilityEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (invisibilityEffect == null) {
            LOGGER.atWarning().log("invisibility: %s effect asset not found", effectId);
            return false;
        }

        EffectControllerComponent controller = accessor.getComponent(
                ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.addEffect(ref, invisibilityEffect, durationSeconds,
                    OverlapBehavior.OVERWRITE, accessor);
        }

        InvisibilityState existing = ConstructStateUtil.findState(
                accessor, ref, InvisibilityGlyph.ID, InvisibilityState.class);
        if (existing != null) {
            existing.setRemainingDuration(durationSeconds);
            existing.setNextGlyphIds(glyph.getNextLinks());
        } else {
            InvisibilityState state = new InvisibilityState(durationSeconds, effectId, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, InvisibilityGlyph.ID, state);
        }

        HexAppearanceService.addLayer(accessor, ref, InvisibilityGlyph.ID,
                AppearanceLayer.ofNameplateHidden());

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            InvisibilityStyle.renderEntityHit(tc.getPosition(), hexContext, accessor);
        }

        LOGGER.atInfo().log("invisibility: applied effect for %.1fs to entity", durationSeconds);
        return true;
    }
}
