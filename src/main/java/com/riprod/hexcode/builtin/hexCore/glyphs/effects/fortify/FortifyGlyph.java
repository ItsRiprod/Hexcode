package com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify;

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
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify.style.FortifyStyle;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class FortifyGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() { return ID; }

    public static final String ID = "Fortify";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(FortifyConfig.class, FortifyConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(FortifyGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        FortifyConfig config = getConfig(FortifyConfig.class, asset);
        if (config == null) config = FortifyConfig.DEFAULTS;

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(FortifyGlyphSlots.DURATION, hexContext),
                asset.getSlot(FortifyGlyphSlots.DURATION));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        boolean sustained = entityVar != null
                && applyToEntities(glyph, entityVar, (float) duration, config, hexContext, accessor);

        // sustained cast defers continuation to the construct's onEnd; otherwise pass through
        if (!sustained) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }

    private boolean applyToEntities(Glyph glyph, EntityVar entityVar, float durationSeconds,
            FortifyConfig config, HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) return false;

        String effectId = config.getEffectId();
        EntityEffect fortifyEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (fortifyEffect == null) {
            LOGGER.atWarning().log("fortify: %s effect asset not found", effectId);
            return false;
        }

        EffectControllerComponent controller = accessor.getComponent(
                ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.addEffect(ref, fortifyEffect, durationSeconds,
                    OverlapBehavior.OVERWRITE, accessor);
        }

        FortifyState existing = ConstructStateUtil.findState(
                accessor, ref, FortifyGlyph.ID, FortifyState.class);
        if (existing != null) {
            existing.setRemainingDuration(durationSeconds);
            existing.setNextGlyphIds(glyph.getNextLinks());
        } else {
            FortifyState state = new FortifyState(durationSeconds, effectId, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, FortifyGlyph.ID, state);
        }

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            FortifyStyle.renderEntityHit(tc.getPosition(), hexContext, accessor);
        }

        LOGGER.atInfo().log("fortify: applied resistance effect for %.1fs to entity", durationSeconds);
        return true;
    }
}
