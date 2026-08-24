package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.utils.HexVarUtil;

import java.util.Arrays;

public class MageArmorGlyph implements GlyphHandler {

    public static final String ID = "MageArmor";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(MageArmorConfig.class, MageArmorConfig.CODEC);
    }


    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Mage Armor must target an entity");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        MageArmorConfig config = getConfig(MageArmorConfig.class, asset);
        if (config == null) config = MageArmorConfig.DEFAULTS;

        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();

        HexVar durationVar = glyph.readSlot(MageArmorGlyphSlots.DURATION, hexContext);
        float duration = HexVarUtil.numberOrSlotDefault(
                durationVar, asset.getSlot(MageArmorGlyphSlots.DURATION)).floatValue();

        String effectId = config.getStatusEffect();
        accessor.putComponent(target, MagicHealthComponent.getComponentType(),
                new MagicHealthComponent());
        ElementSupport.applyStatus(hexContext, target, glyph, effectId, duration);

        MageArmorState existing = ConstructStateUtil.findState(
                accessor, target, MageArmorGlyph.ID, MageArmorState.class);
        if (existing != null) {
            existing.refresh(duration, glyph.getNextLinks());
        } else {
            MageArmorState state = new MageArmorState(effectId, duration, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, target, hexContext, glyph, MageArmorGlyph.ID, state);
        }

        Slot immediate = glyph.getSlot(MageArmorGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
