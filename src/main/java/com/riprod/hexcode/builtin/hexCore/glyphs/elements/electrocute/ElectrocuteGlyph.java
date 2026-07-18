package com.riprod.hexcode.builtin.hexCore.glyphs.elements.electrocute;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.protection.HexProtection;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

import java.util.Arrays;

public class ElectrocuteGlyph implements GlyphHandler {

    public static final String ID = "Electrocute";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ElectrocuteConfig.class, ElectrocuteConfig.CODEC);
    }


    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Electrocute must target an entity");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ElectrocuteConfig config = getConfig(ElectrocuteConfig.class, asset);
        if (config == null) config = ElectrocuteConfig.DEFAULTS;

        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float limit = ElementSupport.resourceLimit(glyph, asset, hexContext);
        float seconds = ElementSupport.scaledDuration(ElementSupport.consumeResource(hexContext, config.getResource(), limit),
                config.getEfficiency(), config.getDurationPerComplexity(),
                affinity);

        String effectId = config.getStatusEffect();
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (!ElementSupport.applyStatus(target, accessor, effectId, seconds)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Electrocute could not apply " + effectId);
            return;
        }

        ElectrocuteState existing = ConstructStateUtil.findState(
                accessor, target, ElectrocuteGlyph.ID, ElectrocuteState.class);
        if (existing != null) {
            existing.refresh(seconds, glyph.getNextLinks());
        } else {
            ElectrocuteState state = new ElectrocuteState(effectId, seconds, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, target, hexContext, glyph, ElectrocuteGlyph.ID, state);
        }

        Slot immediate = glyph.getSlot(ElectrocuteGlyphSlots.IMMEDIATE);
        if (immediate != null && immediate.getLinks().length > 0) {
            HexContext immediateCtx = hexContext.branch();
            HexExecuter.continueExecution(Arrays.asList(immediate.getLinks()), immediateCtx);
        }
    }
}
