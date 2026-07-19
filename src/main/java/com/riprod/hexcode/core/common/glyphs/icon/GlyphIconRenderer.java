package com.riprod.hexcode.core.common.glyphs.icon;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class GlyphIconRenderer {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private GlyphIconRenderer() {
    }

    @Nullable
    public static byte[] render(@Nonnull String glyphId) {
        GlyphAsset glyph = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (glyph == null) {
            return null;
        }
        String modelPath = glyph.getModelPath();
        if (modelPath == null || modelPath.isEmpty()) {
            modelPath = glyphId;
        }

        return GlyphIconRasterizer.rasterize(modelPath, GlyphIconRenderer::resolveExisting);
    }

    // first loaded pack containing the given pack-relative path
    @Nullable
    private static Path resolveExisting(String relPath) {
        for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            Path candidate = pack.getRoot().resolve(relPath);
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
