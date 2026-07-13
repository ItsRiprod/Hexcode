package com.riprod.hexcode.core.common.drawing.system.shapes;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class DirectProjectionDetector implements ShapeDetector {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final int GRID = 32;
    private static final int RADIUS = 3;
    private static final float PRECISION_WEIGHT = 0.7f;
    private static final float RECALL_WEIGHT = 0.3f;
    private static final int CENTER_MIN = 13;
    private static final int CENTER_MAX = 19;
    private static final int RESAMPLE_COUNT = 64;

    @Override
    public DrawnShapeComponent detect(FloatArrayList points, float minYaw, float maxYaw, float minPitch,
            float maxPitch) {
        float[][] normalized = normalizePoints(points, minYaw, maxYaw, minPitch, maxPitch);
        float[][] resampled = resamplePath(normalized, RESAMPLE_COUNT);

        List<String> names = new ArrayList<>();
        List<Float> scores = new ArrayList<>();
        float bestScore = -1f;
        ShapeAsset bestMatch = null;

        for (var entry : ShapeAsset.getAssetMap().getAssetMap().entrySet()) {
            ShapeAsset asset = entry.getValue();
            float score = scoreAgainst(normalized, resampled, asset);
            names.add(entry.getKey());
            scores.add(score);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = asset;
            }
        }

        logTopCandidates(names, scores);

        return bestMatch != null ? new DrawnShapeComponent(bestMatch.getId(), bestScore, bestMatch) : null;
    }

    private float scoreAgainst(float[][] normalized, float[][] resampled, ShapeAsset asset) {
        if (!centerCheck(normalized, asset))
            return 0f;

        int[][] templateDT = ShapeCacheManager.getDistanceTransform(asset.getId());
        boolean[][] template = ShapeCacheManager.getImageData(asset.getId());
        if (templateDT == null || template == null)
            return 0f;

        float score = computeScore(normalized, resampled, template, templateDT);

        if (Boolean.TRUE.equals(asset.getCanRotate())) {
            float[][] rotNorm = rotate90(normalized);
            float[][] rotResampled = resamplePath(rotNorm, RESAMPLE_COUNT);
            score = Math.max(score, computeScore(rotNorm, rotResampled, template, templateDT));
        }

        return score;
    }

    private float computeScore(float[][] normalized, float[][] resampled,
            boolean[][] template, int[][] templateDT) {
        float precision = 0f;
        int precisionCount = 0;

        for (float[] p : normalized) {
            int x = Math.clamp(Math.round(p[0]), 0, GRID - 1);
            int y = Math.clamp(Math.round(p[1]), 0, GRID - 1);
            precision += Math.max(0f, 1f - (float) templateDT[y][x] / RADIUS);
            precisionCount++;
        }

        float recall = 0f;
        int recallCount = 0;

        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                if (!template[y][x]) continue;
                recallCount++;
                float minDist = Float.MAX_VALUE;
                for (float[] r : resampled) {
                    float dx = x - r[0];
                    float dy = y - r[1];
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < minDist) minDist = dist;
                }
                recall += Math.max(0f, 1f - minDist / RADIUS);
            }
        }

        if (precisionCount == 0 || recallCount == 0) return 0f;

        precision /= precisionCount;
        recall /= recallCount;

        return PRECISION_WEIGHT * precision + RECALL_WEIGHT * recall;
    }

    private boolean centerCheck(float[][] normalized, ShapeAsset asset) {
        if (asset.getCenterFilled() == null)
            return true;

        boolean hasCenter = false;
        for (float[] p : normalized) {
            int x = Math.clamp(Math.round(p[0]), 0, GRID - 1);
            int y = Math.clamp(Math.round(p[1]), 0, GRID - 1);
            if (x >= CENTER_MIN && x < CENTER_MAX && y >= CENTER_MIN && y < CENTER_MAX) {
                hasCenter = true;
                break;
            }
        }

        return hasCenter == asset.getCenterFilled();
    }

    private float[][] normalizePoints(FloatArrayList points, float minYaw, float maxYaw,
            float minPitch, float maxPitch) {
        float yawSpan = maxYaw - minYaw;
        float pitchSpan = maxPitch - minPitch;
        float maxSpan = Math.max(yawSpan, pitchSpan);
        if (maxSpan < 0.001f) maxSpan = 1f;

        float yawOffset = (maxSpan - yawSpan) / 2f;
        float pitchOffset = (maxSpan - pitchSpan) / 2f;

        int count = points.size() / 2;
        float[][] result = new float[count][2];
        for (int i = 0; i < count; i++) {
            result[i][0] = ((points.getFloat(i * 2) - minYaw + yawOffset) / maxSpan) * (GRID - 1);
            result[i][1] = ((points.getFloat(i * 2 + 1) - minPitch + pitchOffset) / maxSpan) * (GRID - 1);
        }
        return result;
    }

    private float[][] rotate90(float[][] points) {
        float[][] rotated = new float[points.length][2];
        float center = (GRID - 1) / 2f;
        for (int i = 0; i < points.length; i++) {
            rotated[i][0] = center + (points[i][1] - center);
            rotated[i][1] = center - (points[i][0] - center);
        }
        return rotated;
    }

    private float[][] resamplePath(float[][] points, int n) {
        if (points.length < 2) return points;

        float[] arcLengths = new float[points.length];
        arcLengths[0] = 0;
        for (int i = 1; i < points.length; i++) {
            float dx = points[i][0] - points[i - 1][0];
            float dy = points[i][1] - points[i - 1][1];
            arcLengths[i] = arcLengths[i - 1] + (float) Math.sqrt(dx * dx + dy * dy);
        }

        float totalLength = arcLengths[arcLengths.length - 1];
        if (totalLength < 0.001f) return points;

        float spacing = totalLength / (n - 1);
        float[][] result = new float[n][2];
        result[0] = new float[] { points[0][0], points[0][1] };

        int srcIdx = 1;
        for (int i = 1; i < n; i++) {
            float targetDist = i * spacing;
            while (srcIdx < arcLengths.length - 1 && arcLengths[srcIdx] < targetDist) {
                srcIdx++;
            }
            float segLen = arcLengths[srcIdx] - arcLengths[srcIdx - 1];
            float t = segLen > 0 ? (targetDist - arcLengths[srcIdx - 1]) / segLen : 0;
            result[i][0] = points[srcIdx - 1][0] + t * (points[srcIdx][0] - points[srcIdx - 1][0]);
            result[i][1] = points[srcIdx - 1][1] + t * (points[srcIdx][1] - points[srcIdx - 1][1]);
        }

        return result;
    }

    private void logTopCandidates(List<String> names, List<Float> scores) {
        List<int[]> indices = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            indices.add(new int[] { i });
        }
        indices.sort((a, b) -> Float.compare(scores.get(b[0]), scores.get(a[0])));

        StringBuilder sb = new StringBuilder("[DirectProjection] top 3:");
        for (int j = 0; j < Math.min(3, indices.size()); j++) {
            int idx = indices.get(j)[0];
            sb.append(String.format(" #%d: %s (%.4f)", j + 1, names.get(idx), scores.get(idx)));
            if (j < 2 && j < indices.size() - 1) sb.append(" |");
        }
        LOGGER.atFine().log(sb.toString());
    }

    @Override
    public String getName() {
        return "DirectProjection";
    }

    @Override
    public void ensureLoaded() {
        ShapeCacheManager.ensureLoaded();
    }

    @Override
    public void clearCache() {
        // uses ShapeCacheManager cache, cleared via RasterShapeDetector
    }
}
