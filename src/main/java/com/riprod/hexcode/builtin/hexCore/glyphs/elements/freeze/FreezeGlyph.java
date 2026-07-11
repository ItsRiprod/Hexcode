package com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.freeze.style.FreezeStyle;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public class FreezeGlyph implements GlyphHandler {

    public static final String ID = "Freeze";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(FreezeConfig.class, FreezeConfig.CODEC);
    }

    @Override
    public float getComplexity(Glyph glyph, HexContext hexContext, GlyphAsset asset) {
        return 0f;
    }

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Freeze must target an entity");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        FreezeConfig config = getConfig(FreezeConfig.class, asset);
        if (config == null) config = FreezeConfig.DEFAULTS;

        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float seconds = ElementSupport.scaledDuration(hexContext.consumeComplexity(),
                config.getEfficiency(), config.getDurationPerComplexity(), affinity);

        String effectId = config.getStatusEffect();
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (!ElementSupport.applyStatus(target, accessor, effectId, seconds)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Freeze could not apply " + effectId);
            return;
        }

        TransformComponent tc = accessor.getComponent(target, TransformComponent.getComponentType());
        if (tc != null) {
            FreezeStyle.renderFreeze(tc.getPosition(), hexContext, accessor);
        }

        FreezeState existing = ConstructStateUtil.findState(
                accessor, target, FreezeGlyph.ID, FreezeState.class);
        if (existing != null) {
            existing.refresh(seconds, glyph.getNextLinks());
        } else {
            FreezeState state = new FreezeState(effectId, seconds, glyph.getNextLinks());
            HexConstructSpawner.applyWithState(
                    accessor, target, hexContext, glyph, FreezeGlyph.ID, state);
        }
    }
}
