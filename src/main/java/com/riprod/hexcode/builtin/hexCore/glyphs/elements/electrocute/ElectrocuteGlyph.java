package com.riprod.hexcode.builtin.hexCore.glyphs.elements.electrocute;

import java.util.Arrays;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphAsset;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;

public class ElectrocuteGlyph extends ElementGlyphAsset {

    public static final String TYPE = "Electrocute";

    private String statusEffect = "Lightning";
    private float durationPerComplexity = 1.0f;

    public static final BuilderCodec<ElectrocuteGlyph> CODEC = BuilderCodec
            .builder(ElectrocuteGlyph.class, ElectrocuteGlyph::new, ElementGlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("StatusEffect", Codec.STRING, true),
                    (c, v) -> c.statusEffect = v, c -> c.statusEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("DurationPerComplexity", Codec.FLOAT, true),
                    (c, v) -> c.durationPerComplexity = v, c -> c.durationPerComplexity)
            .addValidator(Validators.greaterThanOrEqual(0.0f))
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Electrocute must target an entity");
            return;
        }

        float affinity = ElementSupport.affinityFactor(hexContext, affinityStat, affinityScale);
        float limit = ElementSupport.resourceLimit(glyph, this, hexContext);
        float seconds = ElementSupport.scaledDuration(
                ElementSupport.consumeResource(hexContext, glyph, resource, limit),
                efficiency, durationPerComplexity,
                affinity);

        String effectId = statusEffect;
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        HexConstructSpawner.abortExisting(accessor, target, ElectrocuteGlyph.TYPE);
        if (!ElementSupport.applyStatus(hexContext, target, glyph, effectId, seconds)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Electrocute could not apply " + effectId);
            return;
        }

        var state = new ElectrocuteState(effectId, seconds, glyph.getNextLinks());
        HexConstructSpawner.applyWithState(
                accessor, target, hexContext, glyph, ElectrocuteGlyph.TYPE, state);

        Slot immediate = glyph.getSlot(ElectrocuteGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
