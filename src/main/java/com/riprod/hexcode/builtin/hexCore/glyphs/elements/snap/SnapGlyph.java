package com.riprod.hexcode.builtin.hexCore.glyphs.elements.snap;

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

public class SnapGlyph implements GlyphHandler {

    public static final String ID = "Snap";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ConfigBinding<? extends GlyphConfig> getConfigBinding() {
        return ConfigBinding.of(SnapConfig.class, SnapConfig.CODEC);
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
                    "Snap must target an entity");
            return;
        }

        GlyphAsset asset = GlyphAsset.getAssetMap().getAsset(glyph.getGlyphId());
        SnapConfig config = getConfig(SnapConfig.class, asset);
        if (config == null) config = SnapConfig.DEFAULTS;

        float affinity = ElementSupport.affinityFactor(
                hexContext, config.getAffinityStat(), config.getAffinityScale());
        float damage = hexContext.consumeComplexity() * config.getEfficiency() * affinity;

        ElementSupport.dealDamage(target, hexContext.getAccessor(),
                config.getDamageCause(), damage, "ums_snap");

        String onHitEffect = config.getOnHitEffect();
        if (onHitEffect != null && !onHitEffect.isEmpty()) {
            ElementSupport.applyStatus(target, hexContext.getAccessor(),
                    onHitEffect, config.getOnHitDuration());
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
