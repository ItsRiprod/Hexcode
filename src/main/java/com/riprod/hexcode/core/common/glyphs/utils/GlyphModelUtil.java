package com.riprod.hexcode.core.common.glyphs.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;

import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;

public final class GlyphModelUtil {

    private static final ModelAttachment[] EMPTY = new ModelAttachment[0];
    private static final String MODEL_EXTENSION = ".blockymodel";
    private static final Box FALLBACK_BOX = Box.horizontallyCentered(0.5, 0.5, 0.5);

    private GlyphModelUtil() {
    }

    @Nonnull
    public static ModelAttachment[] resolveAttachments(@Nonnull GlyphAsset glyph,
            @Nullable ModelAttachment[] authored) {
        if (authored != null && authored.length > 0) {
            return authored;
        }
        return derive(glyph);
    }

    @Nullable
    public static Model assemble(@Nonnull GlyphAsset shapeSource, float scale) {
        String basePath = shapeSource.getModelPath();
        ModelAsset base = basePath != null ? ModelAsset.getAssetMap().getAsset(basePath) : null;
        if (base == null) {
            return null;
        }

        ModelAttachment[] attachments = resolveAttachments(shapeSource, base.getDefaultAttachments());
        if (attachments.length == 0) {
            return null;
        }

        Model scaled = Model.createScaledModel(base, scale);
        return rebuild(scaled, attachments, scaled.getBoundingBox());
    }

    @Nonnull
    public static Model withDefaultBox(@Nonnull Model model, @Nullable ModelAsset defaultModel) {
        Box box = defaultModel != null && defaultModel.getBoundingBox() != null
                ? defaultModel.getBoundingBox().clone()
                : FALLBACK_BOX.clone();
        return rebuild(model, model.getAttachments(), box);
    }

    @Nonnull
    public static Model rebuild(@Nonnull Model scaled, @Nonnull ModelAttachment[] attachments,
            @Nullable Box box) {
        return new Model(scaled.getModelAssetId(), scaled.getScale(), scaled.getRandomAttachmentIds(),
                attachments, box, scaled.getModel(),
                scaled.getTexture(), scaled.getGradientSet(), scaled.getGradientId(), scaled.getEyeHeight(),
                scaled.getCrouchOffset(), scaled.getSittingOffset(), scaled.getSleepingOffset(),
                scaled.getAnimationSetMap(), scaled.getCamera(), scaled.getLight(), scaled.getParticles(),
                scaled.getTrails(), scaled.getPhysicsValues(), scaled.getDetailBoxes(), scaled.getPhobia(),
                scaled.getPhobiaModelAssetId());
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
