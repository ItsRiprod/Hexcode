package com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify.component.FortifyWardComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify.style.FortifyStyle;
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
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.utils.HexVarUtil;

import java.util.Arrays;

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

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        boolean sustained = entityVar != null
                && applyToEntities(glyph, entityVar, config, hexContext, accessor, asset);

        // sustained cast defers continuation to the construct's onEnd; otherwise pass through
        if (sustained) {
            Slot immediate = glyph.getSlot(FortifyGlyphSlots.IMMEDIATE);
            if (immediate != null && immediate.getLinks().length > 0) {
                HexContext immediateCtx = hexContext.branch();
                immediateCtx.setDefaultVariable(entityVar);
                HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
            }
        } else {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
        }
    }

    private boolean applyToEntities(Glyph glyph, EntityVar entityVar, FortifyConfig config,
            HexContext hexContext, CommandBuffer<EntityStore> accessor, GlyphAsset asset) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) return false;

        String effectId = config.getEffectId();
        EntityEffect fortifyEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (fortifyEffect == null) {
            LOGGER.atWarning().log("fortify: %s effect asset not found", effectId);
            return false;
        }

        // resource consumed 100% at cast regardless of when the ward is spent - the stacking gate
        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float limit = ElementSupport.resourceLimit(glyph, asset, hexContext);
        float spent = ElementSupport.consumeResource(hexContext, glyph, config.getResource(), limit);
        float durationSeconds = spent * config.getEfficiency() * affinity * config.getDurationPerResource();

        VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId, durationSeconds,
                OverlapBehavior.OVERWRITE);

        accessor.putComponent(ref, FortifyWardComponent.getComponentType(), FortifyWardComponent.INSTANCE);

        FortifyState existing = ConstructStateUtil.findState(
                accessor, ref, FortifyGlyph.ID, FortifyState.class);
        if (existing != null) {
            existing.refresh(durationSeconds, glyph.getNextLinks());
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
