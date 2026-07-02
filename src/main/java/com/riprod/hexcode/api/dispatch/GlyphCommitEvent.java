package com.riprod.hexcode.api.dispatch;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class GlyphCommitEvent extends CancellableEcsEvent {

    private final Ref<EntityStore> player;
    private final Glyph glyph;
    private final GlyphAsset asset;
    private final String contextId;

    public GlyphCommitEvent(Ref<EntityStore> player, Glyph glyph, GlyphAsset asset, String contextId) {
        this.player = player;
        this.glyph = glyph;
        this.asset = asset;
        this.contextId = contextId;
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

    public String getContextId() {
        return contextId;
    }
}
