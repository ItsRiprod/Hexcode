package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor;

import java.util.Arrays;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;
import com.riprod.hexcode.utils.VfxUtil;

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
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        var target = glyph.readSlot(MageArmorGlyphSlots.TARGET, hexContext) instanceof EntityVar entityVar
                ? entityVar.getRef(accessor)
                : null;
        if (target == null || !target.isValid()) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Mage Armor must target an entity");
            return;
        }

        HexVar durationVar = glyph.readSlot(MageArmorGlyphSlots.DURATION, hexContext);
        float duration = HexVarUtil.numberOrSlotDefault(
                durationVar, getSlot(MageArmorGlyphSlots.DURATION)).floatValue();

        HexConstructSpawner.abortExisting(accessor, target, MageArmorGlyph.TYPE);

        accessor.putComponent(target, MagicHealthComponent.getComponentType(),
                new MagicHealthComponent());
        var applied = VfxUtil.applyBoundedEffect(hexContext, target, glyph, statusEffect, duration,
                OverlapBehavior.OVERWRITE);

        var state = new MageArmorState(applied ? statusEffect : null, duration, glyph.getNextLinks());
        if (HexConstructSpawner.applyWithState(
                accessor, target, hexContext, glyph, MageArmorGlyph.TYPE, state) == null) {
            return;
        }

        Slot immediate = glyph.getSlot(MageArmorGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
