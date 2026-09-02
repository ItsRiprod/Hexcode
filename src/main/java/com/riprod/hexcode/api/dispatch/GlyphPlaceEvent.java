package com.riprod.hexcode.api.dispatch;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class GlyphPlaceEvent extends CancellableEcsEvent {

    private final Ref<EntityStore> player;
    private final Glyph glyph;
    private final GlyphAsset asset;
    private final ShapeStructure structure;

    public GlyphPlaceEvent(Ref<EntityStore> player, Glyph glyph, GlyphAsset asset,
            @Nullable ShapeStructure structure) {
        this.player = player;
        this.glyph = glyph;
        this.asset = asset;
        this.structure = structure;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }

    public Glyph getGlyph() {
        return glyph;
    }

    public GlyphAsset getAsset() {
        return asset;
    }

    @Nullable
    public ShapeStructure getStructure() {
        return structure;
    }
}
