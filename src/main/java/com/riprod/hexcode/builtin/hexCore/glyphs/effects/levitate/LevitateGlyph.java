package com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate;

import java.util.Arrays;
import java.util.UUID;

import com.hypixel.hytale.codec.Codec;
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
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.levitate.style.LevitateStyle;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

import lombok.Getter;

public class LevitateGlyph extends GlyphAsset {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String TYPE = "Levitate";

    private double durationFloor = 1.0;
    @Getter
    private float tickInterval = 0.5f;
    @Getter
    private double riseSpeedPerIntensity = 1.5;
    @Getter
    private double maxCatchAccel = 40.0;
    private String effectId = "Hexcode_Levitate";

    public static final BuilderCodec<LevitateGlyph> CODEC = BuilderCodec
            .builder(LevitateGlyph.class, LevitateGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("DurationFloor", Codec.DOUBLE, true),
                    (c, v) -> c.durationFloor = v, c -> c.durationFloor)
            .add()
            .append(new KeyedCodec<>("TickInterval", Codec.FLOAT, true),
                    (c, v) -> c.tickInterval = v, c -> c.tickInterval)
            .add()
            .append(new KeyedCodec<>("RiseSpeedPerIntensity", Codec.DOUBLE, true),
                    (c, v) -> c.riseSpeedPerIntensity = v, c -> c.riseSpeedPerIntensity)
            .add()
            .append(new KeyedCodec<>("MaxCatchAccel", Codec.DOUBLE, true),
                    (c, v) -> c.maxCatchAccel = v, c -> c.maxCatchAccel)
            .add()
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        HexVar targets = glyph.readSlot(LevitateGlyphSlots.TARGET, hexContext);
        if (!(targets instanceof EntityVar entityVar)) {

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

        float intensity = HexVarUtil.numberOrSlotDefault(
                glyph.readSlot(LevitateGlyphSlots.INTENSITY, hexContext),
                getSlot(LevitateGlyphSlots.INTENSITY)).floatValue();
        float durationSeconds = (float) Math.max(durationFloor,
                HexVarUtil.numberOrSlotDefault(
                        glyph.readSlot(LevitateGlyphSlots.DURATION, hexContext),
                        getSlot(LevitateGlyphSlots.DURATION)));

        UUID constructId = null;
        try {
            HexConstructSpawner.abortExisting(accessor, ref, LevitateGlyph.TYPE);

            VfxUtil.applyBoundedEffect(hexContext, ref, glyph, effectId, durationSeconds,
                    OverlapBehavior.OVERWRITE);

            var state = new LevitateState();
            state.setAppliedIntensity(intensity);
            state.setRemainingDuration(durationSeconds);
            state.setNextGlyphIds(glyph.getNextLinks());
            state.setEffectId(effectId);
            constructId = HexConstructSpawner.applyWithState(
                    accessor, ref, hexContext, glyph, LevitateGlyph.TYPE, state);
            if (constructId == null) {
                VfxUtil.removeEffect(accessor, ref, effectId);
                return;
            }

            Slot immediate = glyph.getSlot(LevitateGlyphSlots.IMMEDIATE);
            if (immediate != null && immediate.getLinks().length > 0) {
                HexContext immediateCtx = hexContext.branch();
                immediateCtx.setDefaultVariable(entityVar);
                HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
            }

            TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
            if (tc != null) {
                LevitateStyle.renderActivation(this, tc.getPosition(), hexContext, accessor);
            }
        } catch (Exception e) {
            if (constructId != null) {
                LOGGER.atWarning().withCause(e).log("levitate: post-spawn step failed");
                return;
            }
            VfxUtil.removeEffect(accessor, ref, effectId);
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Cannot apply levitate", e);
        }
    }
}
