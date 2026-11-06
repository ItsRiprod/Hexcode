package com.riprod.hexcode.core.common.glyphs.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;

import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.core.common.hexes.component.EncodingStroke;

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
    public static Model assemble(@Nonnull GlyphAsset shapeSource, @Nullable GlyphEffects shapeStyle) {
        return assemble(shapeSource, shapeStyle, (Float) null);
    }

    @Nullable
    public static Model assemble(@Nonnull GlyphAsset shapeSource, @Nullable GlyphEffects shapeStyle,
            float scale) {
        return assemble(shapeSource, shapeStyle, Float.valueOf(scale));
    }

    @Nullable
    private static Model assemble(@Nonnull GlyphAsset shapeSource, @Nullable GlyphEffects shapeStyle,
            @Nullable Float scale) {
        ModelAsset override = overrideModel(shapeStyle);
        if (override != null) {
            return scaled(override, scale);
        }

        String basePath = shapeSource.getModelPath();
        ModelAsset base = basePath != null ? ModelAsset.getAssetMap().getAsset(basePath) : null;
        if (base == null) {
            return null;
        }

        ModelAttachment[] attachments = resolveAttachments(shapeSource, base.getDefaultAttachments());
        if (attachments.length == 0) {
            return null;
        }

        Model scaled = scaled(base, scale);
        return rebuild(scaled, attachments, scaled.getBoundingBox());
    }

    @Nonnull
    private static Model scaled(@Nonnull ModelAsset asset, @Nullable Float scale) {
        return scale != null ? Model.createScaledModel(asset, scale) : Model.createRandomScaleModel(asset);
    }

    @Nullable
    private static ModelAsset overrideModel(@Nullable GlyphEffects style) {
        String id = style != null ? style.getModel() : null;
        return id != null ? ModelAsset.getAssetMap().getAsset(id) : null;
    }

    @Nonnull
    public static Model withDefaultBox(@Nonnull Model model, @Nullable Model own) {
        var box = own != null && own.getBoundingBox() != null
                ? own.getBoundingBox().clone()
                : FALLBACK_BOX.clone();
        return rebuild(model, model.getAttachments(), box);
    }

    @Nonnull
    public static Model rebuild(@Nonnull Model scaled, @Nonnull ModelAttachment[] attachments,
            @Nullable Box box) {
        return rebuild(scaled, attachments, box, scaled.getLight(), scaled.getParticles(), scaled.getTrails());
    }

    @Nonnull
    public static Model rebuild(@Nonnull Model scaled, @Nonnull ModelAttachment[] attachments,
            @Nullable Box box, @Nullable ColorLight light, @Nullable ModelParticle[] particles,
            @Nullable ModelTrail[] trails) {
        return new Model(scaled.getModelAssetId(), scaled.getScale(), scaled.getRandomAttachmentIds(),
                attachments, box, scaled.getModel(),
                scaled.getTexture(), scaled.getGradientSet(), scaled.getGradientId(), scaled.getEyeHeight(),
                scaled.getCrouchOffset(), scaled.getSittingOffset(), scaled.getSleepingOffset(),
                scaled.getAnimationSetMap(), scaled.getCamera(), light, particles,
                trails, scaled.getPhysicsValues(), scaled.getDetailBoxes(), scaled.getPhobia(),
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

    @Nonnull
    public static ModelAttachment[] deriveFromStrokes(@Nullable List<EncodingStroke> strokes) {
        if (strokes == null || strokes.isEmpty()) {
            return EMPTY;
        }

        List<ModelAttachment> attachments = new ArrayList<>(strokes.size());
        for (EncodingStroke stroke : strokes) {
            ShapeAsset shape = ShapeAsset.getAssetMap().getAsset(stroke.getShapeId());
            if (shape == null) {
                continue;
            }
            String texture = shape.getTexture();
            String model = resolveRung(shape.getModel(), rung(stroke.getRelativeSize()));
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
