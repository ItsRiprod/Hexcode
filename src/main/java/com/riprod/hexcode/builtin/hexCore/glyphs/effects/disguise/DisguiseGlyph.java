package com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise;

import java.util.Arrays;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
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
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class DisguiseGlyph implements GlyphHandler {

    public static final String ID = "Disguise";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targetVar = glyph.readSlot(DisguiseGlyphSlots.TARGET, hexContext);
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());

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
        applyToEntity(glyph, hexContext, accessor, targetEntity, disguise, durationSeconds);
    }

    private void applyToEntity(Glyph glyph, HexContext hexContext, CommandBuffer<EntityStore> accessor,
            EntityVar targetEntity, AppearanceLayer disguise, float durationSeconds) {
        Ref<EntityStore> targetRef = targetEntity.getRef(accessor);
        if (targetRef == null || !targetRef.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return;
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
        Ref<EntityStore> referenceRef = referenceEntity.getRef(accessor);
        if (referenceRef == null || !referenceRef.isValid()
                || referenceRef.getStore() != accessor.getExternalData().getStore()) {
            return null;
        }

        ModelComponent modelComp = accessor.getComponent(referenceRef, ModelComponent.getComponentType());
        if (modelComp == null || modelComp.getModel() == null) return null;
        String modelId = modelComp.getModel().getModelAssetId();
        if (modelId == null) return null;

        PlayerSkin skin = null;
        PlayerSkinComponent skinComp = accessor.getComponent(referenceRef, PlayerSkinComponent.getComponentType());
        if (skinComp != null) {
            skin = skinComp.getPlayerSkin();
        }

        return AppearanceLayer.ofModel(modelId, skin);
    }
}
