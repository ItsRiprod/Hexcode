package com.riprod.hexcode.core.common.glyphs.icon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.glyphs.utils.GlyphAttachments;

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
        ModelAsset model = modelId != null && !modelId.isEmpty()
                ? ModelAsset.getAssetMap().getAsset(modelId)
                : null;

        ModelAttachment[] attachments = model != null ? model.getDefaultAttachments() : null;
        if (attachments == null || attachments.length == 0) {
            attachments = GlyphAttachments.derive(glyph);
        }

        if (attachments.length == 0) {
            return null;
        }

        return GlyphIconRasterizer.rasterize(attachments);
    }
}
