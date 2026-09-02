package com.riprod.hexcode.api.dispatch;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public class GlyphResolveEvent extends CancellableEcsEvent {

    private final Ref<EntityStore> player;
    private final ShapeStructure structure;
    private final String contextId;

    private Glyph resolvedGlyph;
    private GlyphAsset resolvedAsset;

    public GlyphResolveEvent(Ref<EntityStore> player, ShapeStructure structure, String contextId) {
        this.player = player;
        this.structure = structure;
        this.contextId = contextId;
    }

    public Ref<EntityStore> getPlayer() {
        return player;
    }

    public ShapeStructure getStructure() {
        return structure;
    }

    public String getContextId() {
        return contextId;
    }

    public void setResolution(Glyph glyph, GlyphAsset asset) {
        this.resolvedGlyph = glyph;
        this.resolvedAsset = asset;
    }

    public boolean isResolved() {
        return resolvedGlyph != null && resolvedAsset != null;
    }

    @Nullable
    public Glyph getResolvedGlyph() {
        return resolvedGlyph;
    }

    @Nullable
    public GlyphAsset getResolvedAsset() {
        return resolvedAsset;
    }
}
