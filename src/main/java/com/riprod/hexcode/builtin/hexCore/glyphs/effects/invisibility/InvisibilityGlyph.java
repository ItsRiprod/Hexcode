package com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility;

import java.util.Arrays;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.appearance.AppearanceLayer;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.effects.component.EffectSlot;
import com.riprod.hexcode.core.common.effects.utils.EffectRenderer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class InvisibilityGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String TYPE = "Invisibility";

    private String effectId = "Hexcode_Invisibility";

    public static final BuilderCodec<InvisibilityGlyph> CODEC = BuilderCodec
            .builder(InvisibilityGlyph.class, InvisibilityGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public HexVar readValue(Glyph glyph, HexContext hexContext) {
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        var targetVar = glyph.readSlot(InvisibilityGlyphSlots.TARGET, hexContext);

        if (!(targetVar instanceof EntityVar entityVar))
            return new NumberVar(0);

        Ref<EntityStore> ref = entityVar != null ? entityVar.getRef(accessor) : null;
        if (ref == null || !ref.isValid())
            return new NumberVar(0);

        InvisibilityState state = ConstructStateUtil.findState(
                accessor, ref, InvisibilityGlyph.TYPE, InvisibilityState.class);

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

        double duration = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(InvisibilityGlyphSlots.DURATION, hexContext),
                getSlot(InvisibilityGlyphSlots.DURATION));

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        if (!(targets instanceof EntityVar entityVar)) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        boolean sustained = entityVar != null
                && applyToEntities(glyph, entityVar, (float) duration, hexContext, accessor);

        // sustained cast defers continuation to the construct's onEnd; otherwise pass
        // through
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
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid())
            return false;

        EntityEffect invisibilityEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (invisibilityEffect == null) {
            LOGGER.atWarning().log("invisibility: %s effect asset not found", effectId);
            return false;
        }

        HexConstructSpawner.abortExisting(accessor, ref, InvisibilityGlyph.TYPE);

        var applied = VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId, durationSeconds,
                OverlapBehavior.OVERWRITE);

        var state = new InvisibilityState(durationSeconds, applied ? effectId : null, glyph.getNextLinks());
        HexConstructSpawner.applyWithState(
                accessor, ref, hexContext, glyph, InvisibilityGlyph.TYPE, state);

        HexAppearanceService.addLayer(accessor, ref, InvisibilityGlyph.TYPE,
                AppearanceLayer.ofNameplateHidden());

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            EffectRenderer.render(EffectSlot.Start, this, hexContext, tc.getPosition(), accessor);
        }

        LOGGER.atFine().log("invisibility: applied effect for %.1fs to entity", durationSeconds);
        return true;
    }
}
