package com.riprod.hexcode.core.common.glyphs.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;

import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class GlyphAttachments {

    private static final ModelAttachment[] EMPTY = new ModelAttachment[0];
    private static final String MODEL_EXTENSION = ".blockymodel";

    private GlyphAttachments() {
    }

    @Nonnull
    public static ModelAttachment[] derive(@Nonnull GlyphAsset glyph) {
        List<ShapeAsset> shapes = glyph.getShapes();
        if (shapes.isEmpty()) {
            return EMPTY;
        }

        List<ModelAttachment> attachments = new ArrayList<>(shapes.size());
        for (ShapeAsset shape : shapes) {
            String texture = shape.getTexture();
            String model = resolveRung(shape.getModel(), rung(shape.getRelativeSize()));
            if (model == null || texture == null) {
                continue;
            }
            attachments.add(new ModelAttachment(model, texture, null, null, 1.0));
        }

        return attachments.isEmpty() ? EMPTY : attachments.toArray(ModelAttachment[]::new);
    }

    private static int rung(float relativeSize) {
        int tenths = Math.clamp(Math.round(relativeSize * 10f), 1, 10);
        return (12 - tenths) / 2;
    }

    @Nullable
    private static String resolveRung(@Nullable String rungOnePath, int rung) {
        if (rungOnePath == null) {
            return null;
        }

        int separator = rungOnePath.lastIndexOf('_');
        if (separator < 0) {
            return null;
        }

        String stem = rungOnePath.substring(0, separator + 1);
        for (int candidate = rung; candidate >= 1; candidate--) {
            String path = stem + candidate + MODEL_EXTENSION;
            if (CommonAssetRegistry.hasCommonAsset(path)) {
                return path;
            }
        }

        return null;
    }
}
