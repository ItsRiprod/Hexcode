package com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate;

import java.util.Arrays;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.construct.registry.ConstructRegistry;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate.style.LevitateStyle;
import com.riprod.hexcode.builtin.hexCore.utils.ConstructSplicer;
import com.riprod.hexcode.core.common.execution.context.HexContext;
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

public class LevitateGlyph implements GlyphHandler {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public String getId() {
        return ID;
    };

    public static final String ID = "Levitate";

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(LevitateConfig.class, LevitateConfig.CODEC);
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(LevitateGlyphSlots.TARGET, hexContext);
        EntityVar entityVar = HexVarUtil.resolveEntityVar(targets, hexContext);
        if (entityVar == null) {
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        Ref<EntityStore> ref = entityVar.getRef(accessor);
        if (ref == null || !ref.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Target is no longer available");
            return;
        }

        Ref<EntityStore> caster = hexContext.getCasterRef(accessor);
        if (!HexProtection.canAffectEntity(accessor.getExternalData().getWorld(), caster, accessor, ref)) {
            HexProtection.notifyBlocked(caster, accessor, getId());
            HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        LevitateConfig config = getConfig(LevitateConfig.class, asset);
        if (config == null) config = LevitateConfig.DEFAULTS;

        float intensity = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(LevitateGlyphSlots.INTENSITY, hexContext),
                asset.getSlot(LevitateGlyphSlots.INTENSITY)).floatValue();
        float durationSeconds = (float) Math.max(config.getDurationFloor(),
                HexVarUtil.numberOrSlotDefault(
                        glyph.readSlot(LevitateGlyphSlots.DURATION, hexContext),
                        asset.getSlot(LevitateGlyphSlots.DURATION)));

        try {
            String effectId = config.getEffectId();
            VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId, durationSeconds,
                    OverlapBehavior.OVERWRITE);

            HexEffectsComponent construct = accessor.getComponent(
                    ref, HexEffectsComponent.getComponentType());
            HexStatus<?> active = construct != null ? findActiveLevitate(construct) : null;

            if (active != null && active.getState() instanceof LevitateState existing) {
                existing.setAppliedIntensity(intensity);
                existing.setRemainingDuration(durationSeconds);
                existing.setEffectId(effectId);
                ConstructHandler<?> handler = ConstructRegistry.get(active.getHandlerId());
                if (handler != null) {
                    ConstructSplicer.splice(active, handler, hexContext, glyph,
                            ConstructSplicer.ChainMode.APPEND_TAIL,
                            ConstructSplicer.VariablePolicy.PREFER_TARGET, 0f);
                }
            } else {
                LevitateState state = new LevitateState();
                state.setAppliedIntensity(intensity);
                state.setRemainingDuration(durationSeconds);
                state.setNextGlyphIds(glyph.getNextLinks());
                state.setEffectId(effectId);
                HexConstructSpawner.applyWithState(
                        accessor, ref, hexContext, glyph, LevitateGlyph.ID, state);
            }

            Slot immediate = glyph.getSlot(LevitateGlyphSlots.IMMEDIATE);
            if (immediate != null && immediate.getLinks().length > 0) {
                HexContext immediateCtx = hexContext.branch();
                immediateCtx.setDefaultVariable(entityVar);
                HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
            }

            TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
            if (tc != null) {
                LevitateStyle.renderActivation(tc.getPosition(), hexContext, accessor);
            }
        } catch (Exception e) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot apply levitate", e);
        }
    }

    private static HexStatus<?> findActiveLevitate(HexEffectsComponent construct) {
        for (HexStatus<?> status : construct.getEffects().values()) {
            if (status != null && ID.equals(status.getHandlerId())) {
                return status;
            }
        }
        return null;
    }
}
