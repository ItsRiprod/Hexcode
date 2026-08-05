package com.riprod.hexcode.builtin.hexCore.obelisks.accuracy;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.obelisk.interfaces.ObeliskInterface;

public class AccuracyObelisk implements ObeliskInterface {

    private static final float VOLATILITY_MULTIPLIER = 1.3f;
    private static final float EFFICIENCY_MULTIPLIER = 0.7f;

    public void onGlyphDrawn(ComponentAccessor<EntityStore> buffer, Ref<EntityStore> playerRef,
            Glyph glyph, ObeliskBlockComponent obelisk) {
        glyph.setVolatility(Math.min(1, glyph.getVolatility() * VOLATILITY_MULTIPLIER));
        glyph.setEfficiency(glyph.getEfficiency() * EFFICIENCY_MULTIPLIER);
    }
}
