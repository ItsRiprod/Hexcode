package com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise;

import java.util.Arrays;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.components.DisguiseState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise.style.DisguiseStyle;
import com.riprod.hexcode.core.common.appearance.AppearanceLayer;
import com.riprod.hexcode.core.common.appearance.HexAppearanceService;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.VfxUtil;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.HexRefs;

public class DisguiseGlyph implements GlyphHandler {

    public static final String ID = "Disguise";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(DisguiseConfig.class, DisguiseConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(DisguiseGlyphSlots.TARGET, hexContext);
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());

        DisguiseConfig config = getConfig(DisguiseConfig.class, asset);
        if (config == null) config = DisguiseConfig.DEFAULTS;

        AppearanceLayer disguise = resolveDisguise(glyph, hexContext, accessor);
        if (disguise == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Reference must be a creature");
            return;
        }

        float durationSeconds = (float) Math.max(0.0, HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(DisguiseGlyphSlots.DURATION, hexContext),
                asset.getSlot(DisguiseGlyphSlots.DURATION)));

        EntityVar targetEntity = HexVarUtil.resolveEntityVar(targetVar, hexContext);
        if (targetEntity == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target must be a creature");
            return;
        }
        applyToEntity(glyph, hexContext, accessor, targetEntity, disguise, durationSeconds, config);
    }

    private void applyToEntity(Glyph glyph, HexContext hexContext, CommandBuffer<EntityStore> accessor,
            EntityVar targetEntity, AppearanceLayer disguise, float durationSeconds, DisguiseConfig config) {
        Ref<EntityStore> targetRef = targetEntity.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return;
        }

        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (!HexProtection.canAffectEntity(accessor.getExternalData().getWorld(), caster, accessor, targetRef)) {
            HexProtection.notifyBlocked(caster, accessor, ID);
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        if (!config.disguiseNametag()) {
            Nameplate targetNameplate = accessor.getComponent(targetRef, Nameplate.getComponentType());
            disguise = AppearanceLayer.ofModel(disguise.modelAssetId(), disguise.skin(),
                    targetNameplate != null ? targetNameplate.getText() : AppearanceLayer.NAMEPLATE_HIDDEN,
                    disguise.baseScale());
        }

        DisguiseState state = ConstructStateUtil.findState(
                accessor, targetRef, ID, DisguiseState.class);
        boolean isNew = state == null;
        if (isNew) {
            state = new DisguiseState();
        }
        state.setRemainingSeconds(durationSeconds);
        state.setNextGlyphIds(glyph.getNextLinks());

        boolean applied = HexAppearanceService.addLayer(accessor, targetRef,
                state.getConstructId().toString(), disguise);
        if (!applied) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot resolve target model");
            return;
        }

        String effectId = config.getDisguiseEffectId();
        boolean effectApplied = VfxUtil.applyBoundedEffect(hexContext, targetRef, glyph, effectId,
                durationSeconds, OverlapBehavior.OVERWRITE);
        state.setEffectId(effectApplied ? effectId : null);

        if (isNew) {
            HexConstructSpawner.applyWithState(accessor, targetRef, hexContext, glyph, ID, state);
        }

        fireImmediate(glyph, hexContext, targetEntity);

        TransformComponent targetTransform = accessor.getComponent(
                targetRef, TransformComponent.getComponentType());
        Vector3d spawnPos = targetTransform != null
                ? new Vector3d(targetTransform.getPosition()) : new Vector3d();
        DisguiseStyle.renderApply(spawnPos, hexContext, accessor);
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
        EntityVar referenceEntity = HexVarUtil.resolveEntityVar(referenceVar, hexContext);
        if (referenceEntity == null) {
            return null;
        }
        return resolveEntityDisguise(referenceEntity, accessor);
    }

    private AppearanceLayer resolveEntityDisguise(EntityVar referenceEntity,
            CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> referenceRef = HexRefs.live(referenceEntity.getRef(accessor), accessor);
        if (referenceRef == null) {
            return null;
        }

        ModelComponent modelComp = accessor.getComponent(referenceRef, ModelComponent.getComponentType());
        if (modelComp == null || modelComp.getModel() == null) return null;
        String modelId = modelComp.getModel().getModelAssetId();
        if (modelId == null) return null;

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
