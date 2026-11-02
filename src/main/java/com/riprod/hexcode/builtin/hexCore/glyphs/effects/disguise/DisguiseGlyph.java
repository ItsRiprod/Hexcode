package com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise;

import java.util.Arrays;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.components.DisguiseState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.style.DisguiseStyle;
import com.riprod.hexcode.core.common.appearance.AppearanceLayer;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.HexRefs;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class DisguiseGlyph extends GlyphAsset {

    public static final String TYPE = "Disguise";

     private boolean disguiseNametag = true;
    private String disguiseEffectId;

    public static final BuilderCodec<DisguiseGlyph> CODEC = BuilderCodec
            .builder(DisguiseGlyph.class, DisguiseGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("DisguiseNametag", Codec.BOOLEAN, true),
                    (c, v) -> c.disguiseNametag = v, c -> c.disguiseNametag)
            .add()
            .append(new KeyedCodec<>("DisguiseEffect", Codec.STRING, true),
                    (c, v) -> c.disguiseEffectId = v, c -> c.disguiseEffectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(DisguiseGlyphSlots.TARGET, hexContext);
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        AppearanceLayer disguise = resolveDisguise(glyph, hexContext, accessor);
        if (disguise == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Reference must be a creature");
            return;
        }

        float durationSeconds = (float) Math.max(0.0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(DisguiseGlyphSlots.DURATION, hexContext),
                getSlot(DisguiseGlyphSlots.DURATION)));

        if (!(targetVar instanceof EntityVar entityVar)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be a creature");
            return;

        }

        applyToEntity(glyph, hexContext, accessor, entityVar, disguise, durationSeconds);
    }

    private void applyToEntity(Glyph glyph, HexContext hexContext, CommandBuffer<EntityStore> accessor,
            EntityVar targetEntity, AppearanceLayer disguise, float durationSeconds) {
        Ref<EntityStore> targetRef = targetEntity.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return;
        }

        Ref<EntityStore> caster = hexContext.getRootRef();
        if (!HexProtection.canAffectEntity(accessor.getExternalData().getWorld(), caster, accessor, targetRef)) {
            HexProtection.notifyBlocked(caster, accessor, TYPE);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        HexConstructSpawner.abortExisting(accessor, targetRef, TYPE);

        if (!disguiseNametag) {
            Nameplate targetNameplate = accessor.getComponent(targetRef, Nameplate.getComponentType());
            disguise = AppearanceLayer.ofModel(disguise.modelAssetId(), disguise.skin(),
                    targetNameplate != null ? targetNameplate.getText() : AppearanceLayer.NAMEPLATE_HIDDEN,
                    disguise.baseScale());
        }

        var state = new DisguiseState();
        var applied = HexAppearanceService.addLayer(accessor, targetRef,
                state.getConstructId().toString(), disguise);
        if (!applied) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot resolve target model");
            return;
        }

        state.setRemainingSeconds(durationSeconds);
        state.setNextGlyphIds(glyph.getNextLinks());
        var effectApplied = VfxUtil.applyBoundedEffect(hexContext, targetRef, glyph, disguiseEffectId,
                durationSeconds, OverlapBehavior.OVERWRITE);
        state.setEffectId(effectApplied ? disguiseEffectId : null);

        HexConstructSpawner.applyWithState(accessor, targetRef, hexContext, glyph, TYPE, state);

        fireImmediate(glyph, hexContext, targetEntity);

        TransformComponent targetTransform = accessor.getComponent(
                targetRef, TransformComponent.getComponentType());
        Vector3d spawnPos = targetTransform != null
                ? new Vector3d(targetTransform.getPosition())
                : new Vector3d();
        DisguiseStyle.renderApply(this, spawnPos, hexContext, accessor);
    }

    private void fireImmediate(Glyph glyph, HexContext hexContext, HexVar defaultVar) {
        Slot immediate = glyph.getSlot(DisguiseGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            immediateCtx.setDefaultVariable(defaultVar);
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }

    private AppearanceLayer resolveDisguise(Glyph glyph, HexContext hexContext,
            CommandBuffer<EntityStore> accessor) {
        HexVar referenceVar = glyph.readSlot(DisguiseGlyphSlots.REFERENCE, hexContext);
        if (!(referenceVar instanceof EntityVar entityVar)) {
            return null;
        }

        return resolveEntityDisguise(entityVar, accessor);
    }

    private AppearanceLayer resolveEntityDisguise(EntityVar referenceEntity,
            CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> referenceRef = HexRefs.live(referenceEntity.getRef(accessor), accessor);
        if (referenceRef == null) {
            return null;
        }

        ModelComponent modelComp = accessor.getComponent(referenceRef, ModelComponent.getComponentType());
        if (modelComp == null || modelComp.getModel() == null)
            return null;
        String modelId = modelComp.getModel().getModelAssetId();
        if (modelId == null)
            return null;

        float referenceScale = modelComp.getModel().getScale();
        EntityScaleComponent scaleComp = accessor.getComponent(
                referenceRef, EntityScaleComponent.getComponentType());
        if (scaleComp != null && scaleComp.getScale() > 0f) {
            referenceScale = scaleComp.getScale();
        }
        if (referenceScale <= 0f) {
            referenceScale = 1.0f;
        }

        PlayerSkin skin = null;
        PlayerSkinComponent skinComp = accessor.getComponent(referenceRef, PlayerSkinComponent.getComponentType());
        if (skinComp != null) {
            skin = skinComp.getPlayerSkin();
        }

        String nameplate = AppearanceLayer.NAMEPLATE_HIDDEN;
        Nameplate nameplateComp = accessor.getComponent(referenceRef, Nameplate.getComponentType());
        if (nameplateComp != null && !nameplateComp.getText().isEmpty()) {
            nameplate = nameplateComp.getText();
        }

        return AppearanceLayer.ofModel(modelId, skin, nameplate, referenceScale);
    }
}
