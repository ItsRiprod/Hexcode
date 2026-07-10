package com.riprod.hexcode.builtin.hexCore.glyphs.elements.drench;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.core.common.construct.system.HexConstructSpawner;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public class DrenchGlyph implements GlyphHandler {

    public static final String ID = "Drench";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(DrenchConfig.class, DrenchConfig.CODEC);
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
                    "Drench must target an entity");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        DrenchConfig config = getConfig(DrenchConfig.class, asset);
        if (config == null) config = DrenchConfig.DEFAULTS;

        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float seconds = ElementSupport.scaledDuration(hexContext.consumeComplexity(),
                config.getEfficiency(), config.getDurationPerComplexity(),
                config.getMinDuration(), config.getMaxDuration(), affinity);

        String effectId = config.getStatusEffect();
        CommandBuffer<EntityStore> accessor = hexContext.getAccessor();
        if (!ElementSupport.applyStatus(target, accessor, effectId, seconds)) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Drench could not apply " + effectId);
            return;
        }

        DrenchState state = new DrenchState(effectId, seconds, glyph.getNextLinks());
        HexConstructSpawner.applyWithState(
                accessor, target, hexContext, glyph, DrenchGlyph.ID, state);
    }
}
