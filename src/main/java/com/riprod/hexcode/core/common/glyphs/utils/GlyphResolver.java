package com.riprod.hexcode.core.common.glyphs.utils;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.GlyphCommitEvent;
import com.riprod.hexcode.api.dispatch.GlyphResolveEvent;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.core.common.drawing.system.GlyphCreationManager;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class GlyphResolver {

    public enum Status {
        RESOLVED,
        NO_MATCH,
        VETOED
    }

    public record Resolution(Status status, @Nullable Glyph glyph, @Nullable GlyphAsset asset) {

        private static final Resolution NO_MATCH = new Resolution(Status.NO_MATCH, null, null);
        private static final Resolution VETOED = new Resolution(Status.VETOED, null, null);

        public boolean isResolved() {
            return status == Status.RESOLVED;
        }
    }

    private GlyphResolver() {
    }

    @Nullable
    public static GlyphAsset resolve(ShapeStructure structure) {
        if (structure == null || structure.getShapes().isEmpty()) {
            return null;
        }
        return GlyphCreationManager.MatchGlyph(structure.getShapes());
    }

    public static Resolution resolve(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            ShapeStructure structure, String contextId) {
        var resolve = new GlyphResolveEvent(player, structure, contextId);
        buffer.invoke(player, resolve);
        if (resolve.isCancelled()) {
            return Resolution.VETOED;
        }

        Glyph glyph = resolve.getResolvedGlyph();
        GlyphAsset asset = resolve.getResolvedAsset();
        if (glyph == null || asset == null) {
            asset = resolve(structure);
            if (asset == null) {
                return Resolution.NO_MATCH;
            }
            glyph = new Glyph(asset, structure.getVolatility(), structure.getEfficiency());
        }

        var commit = new GlyphCommitEvent(player, glyph, asset, contextId);
        buffer.invoke(player, commit);
        if (commit.isCancelled()) {
            return Resolution.VETOED;
        }
        return new Resolution(Status.RESOLVED, glyph, asset);
    }
}
