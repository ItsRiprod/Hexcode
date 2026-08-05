package com.riprod.hexcode.core.common.drawing.system;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphAsset;
import com.riprod.hexcode.utils.LogScopes;

public class GlyphCreationManager {
    public static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.DRAW);

    public static void NormalizeShapeSizes(List<DrawnShapeComponent> drawn) {
        float maxSize = 0f;
        for (DrawnShapeComponent shape : drawn) {
            maxSize = Math.max(maxSize, shape.getSize());
        }

        for (DrawnShapeComponent shape : drawn) {
            shape.setRelativeSize(shape.getSize() / maxSize);
        }
    }

    public static float ScoreAsset(List<DrawnShapeComponent> drawn, List<ShapeAsset> asset) {
        if (drawn.size() != asset.size())
            return 0f;

        float score = 0f;
        for (int i = 0; i < drawn.size(); i++) {
            DrawnShapeComponent d = drawn.get(i);
            ShapeAsset a = asset.get(i);

            if (!d.getShapeId().equals(a.getBaseShapeId()))
                return 0f;

            float sizeDiff = Math.abs(d.getRelativeSize() - a.getRelativeSize());
            score += 1.0f - sizeDiff;
        }

        return score / drawn.size();
    }

    @Nullable
    public static GlyphAsset MatchGlyph(List<DrawnShapeComponent> drawn) {
        Map<String, GlyphAsset> assetMap = GlyphAsset.getAssetMap().getAssetMap();

        GlyphAsset bestMatch = null;
        float bestScore = 0f;
        float threshold = 0.7f;

        for (GlyphAsset asset : assetMap.values()) {
            float score = ScoreAsset(drawn, asset.getShapes());
            if (score > bestScore && score >= threshold) {
                bestScore = score;
                bestMatch = asset;
            }
            if (score > 0f) {
                LOGGER.atFine().log("Scored glyph '%s' with %.2f accuracy", asset.getId(), score);
            }
        }

        LOGGER.atFine().log("Best glyph match: " + (bestMatch != null ? bestMatch.getId() : "none") + " with score " + bestScore);

        return bestMatch; // null if nothing matched
    }
}
