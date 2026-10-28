package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor;

import java.util.Arrays;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

public class MageArmorGlyph extends GlyphAsset {

    public static final String TYPE = "MageArmor";

    private String statusEffect = "Hexcode_Mage_Armor";

    public static final BuilderCodec<MageArmorGlyph> CODEC = BuilderCodec
            .builder(MageArmorGlyph.class, MageArmorGlyph::new, GlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("StatusEffect", Codec.STRING, true),
                    (c, v) -> c.statusEffect = v, c -> c.statusEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Mage Armor must target an entity");
            return;
        }

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        HexVar durationVar = glyph.readSlot(MageArmorGlyphSlots.DURATION, hexContext);
        float duration = HexVarUtil.numberOrSlotDefault(
                durationVar, getSlot(MageArmorGlyphSlots.DURATION)).floatValue();

        String effectId = statusEffect;
        accessor.putComponent(target, MagicHealthComponent.getComponentType(),
                new MagicHealthComponent());
        ElementSupport.applyStatus(hexContext, target, glyph, effectId, duration);

        MageArmorState existing = ConstructStateUtil.findState(
                accessor, target, MageArmorGlyph.TYPE, MageArmorState.class);
        if (existing != null) {
            existing.refresh(duration, glyph.getNextLinks());
        } else {
            MageArmorState state = new MageArmorState(effectId, duration, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, target, hexContext, glyph, MageArmorGlyph.TYPE, state);
        }

        Slot immediate = glyph.getSlot(MageArmorGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
