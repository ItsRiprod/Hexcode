package com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify;

import java.util.Arrays;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphAsset;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify.component.FortifyWardComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.fortify.style.FortifyStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.LogScopes;
import com.riprod.hexcode.utils.VfxUtil;

public class FortifyGlyph extends ElementGlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.GLYPH);

    public static final String TYPE = "Fortify";

    private String effectId = "Hexcode_Fortify";
    private float durationPerResource = 1.0f;

    public static final BuilderCodec<FortifyGlyph> CODEC = BuilderCodec
            .builder(FortifyGlyph.class, FortifyGlyph::new, ElementGlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("DurationPerResource", Codec.FLOAT, true),
                    (c, v) -> c.durationPerResource = v, c -> c.durationPerResource)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(FortifyGlyphSlots.TARGET, hexContext);
        if (targets == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target required");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        if (!(targets instanceof EntityVar entityVar)) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        boolean sustained = entityVar != null
                && applyToEntities(glyph, entityVar, hexContext, accessor);

        // sustained cast defers continuation to the construct's onEnd; otherwise pass
        // through
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

    private boolean applyToEntities(Glyph glyph, EntityVar entityVar,
            HexContext hexContext, CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid())
            return false;

        EntityEffect fortifyEffect = EntityEffect.getAssetMap().getAsset(effectId);
        if (fortifyEffect == null) {
            LOGGER.atWarning().log("fortify: %s effect asset not found", effectId);
            return false;
        }

        // resource consumed 100% at cast regardless of when the ward is spent - the
        // stacking gate
        float affinity = ElementSupport.affinityFactor(hexContext, affinityStat, affinityScale);
        float limit = ElementSupport.resourceLimit(glyph, this, hexContext);
        float spent = ElementSupport.consumeResource(hexContext, glyph, resource, limit);
        float durationSeconds = spent * efficiency * affinity * durationPerResource;

        HexConstructSpawner.abortExisting(accessor, ref, FortifyGlyph.TYPE);

        var applied = VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId, durationSeconds,
                OverlapBehavior.OVERWRITE);

        accessor.putComponent(ref, FortifyWardComponent.getComponentType(), FortifyWardComponent.INSTANCE);

        var state = new FortifyState(durationSeconds, applied ? effectId : null, glyph.getNextLinks());
        HexConstructSpawner.applyWithState(
                accessor, ref, hexContext, glyph, FortifyGlyph.TYPE, state);

        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc != null) {
            FortifyStyle.renderEntityHit(this, tc.getPosition(), hexContext, accessor);
        }

        LOGGER.atFine().log("fortify: applied resistance effect for %.1fs to entity", durationSeconds);
        return true;
    }
}
