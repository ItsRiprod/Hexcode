package com.riprod.hexcode.core.common.execution.listener;

import java.util.function.Consumer;

import com.riprod.hexcode.api.event.GlyphEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;

public class GlyphCostListener implements Consumer<GlyphEvent> {

    @Override
    public void accept(GlyphEvent e) {
        var glyph = e.getGlyph();
        var ctx = e.getContext();
        var asset = e.getAsset();

        var base = asset.getVolatilityCost(glyph, ctx, asset);
        var resolved = base * e.getCostMultiplier() + e.getCostFlat();
        e.setResolvedCost(resolved);
        var current = ctx.consumeVolatility(resolved);
        if (!ctx.policy().isBypassVolatilityDepletion() && current <= 0f) {
            e.setCancelled(true);
            HexExecuter.fail(glyph, ctx, GlyphFizzleEvent.Reason.VOLATILITY_DEPLETED);
        }
    }
}
