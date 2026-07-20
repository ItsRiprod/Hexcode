package com.riprod.hexcode.core.common.glyphs.icon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class GlyphIconRenderer {

    private GlyphIconRenderer() {
    }

    @Nullable
    public static byte[] render(@Nonnull String glyphId) {
        GlyphAsset glyph = GlyphAsset.getAssetMap().getAsset(glyphId);
        if (glyph == null) {
            return null;
        }

        String modelId = glyph.getModelPath();
        if (modelId == null || modelId.isEmpty()) {
            modelId = glyphId;
        }

        ModelAsset model = ModelAsset.getAssetMap().getAsset(modelId);
        if (model == null) {
            return null;
        }

        ModelAttachment[] attachments = model.getDefaultAttachments();
        if (attachments == null || attachments.length == 0) {
            return null;
        }

        return GlyphIconRasterizer.rasterize(attachments);
    }
}
