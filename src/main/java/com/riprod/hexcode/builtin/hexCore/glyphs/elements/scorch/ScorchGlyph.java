package com.riprod.hexcode.builtin.hexCore.glyphs.elements.scorch;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.GlyphHandler;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public class ScorchGlyph implements GlyphHandler {

    public static final String ID = "Scorch";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(ScorchConfig.class, ScorchConfig.CODEC);
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
                    "Scorch must target an entity");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        ScorchConfig config = getConfig(ScorchConfig.class, asset);
        if (config == null) config = ScorchConfig.DEFAULTS;

        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float damage = hexContext.consumeComplexity() * config.getEfficiency() * affinity;

        ElementSupport.dealDamage(target, hexContext.getAccessor(),
                config.getDamageCause(), damage, "ums_scorch");

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
