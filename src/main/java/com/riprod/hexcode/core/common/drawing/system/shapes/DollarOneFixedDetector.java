package com.riprod.hexcode.core.common.drawing.system.shapes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.drawing.registry.TemplateAsset;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class DollarOneFixedDetector implements ShapeDetector {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final int N = 64;
    private static final float SQUARE_SIZE = 250f;
    private static final float HALF_DIAGONAL = 0.5f * (float) Math.sqrt(2 * SQUARE_SIZE * SQUARE_SIZE);
    private static final float ANGLE_RANGE = (float) Math.toRadians(15);
    private static final float ANGLE_PRECISION = (float) Math.toRadians(2);
    private static final float PHI = 0.5f * (-1f + (float) Math.sqrt(5));

    private static final Map<String, float[][]> templateCache = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    @Override
    public DrawnShapeComponent detect(FloatArrayList points, float minYaw, float maxYaw, float minPitch,
            float maxPitch) {
        ensureLoaded();

        float[][] input = extractPoints(points);
        float[][] processed = preprocess(input);
        float[][] processedReversed = preprocess(reverse(input));

        List<String> names = new ArrayList<>();
        List<Float> scores = new ArrayList<>();
        float bestScore = -1f;
        ShapeAsset bestMatch = null;

        for (var entry : ShapeAsset.getAssetMap().getAssetMap().entrySet()) {
            float[][] template = templateCache.get(entry.getKey());
            if (template == null) {
                names.add(entry.getKey());
                scores.add(0f);
                continue;
            }

            float dist = distanceAtBestAngle(processed, template, -ANGLE_RANGE, ANGLE_RANGE, ANGLE_PRECISION);
            float score = 1f - dist / HALF_DIAGONAL;

            float distRev = distanceAtBestAngle(processedReversed, template, -ANGLE_RANGE, ANGLE_RANGE, ANGLE_PRECISION);
            score = Math.max(score, 1f - distRev / HALF_DIAGONAL);

            score = Math.max(0f, score);
            names.add(entry.getKey());
            scores.add(score);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = entry.getValue();
            }
        }

        logTopCandidates(names, scores);

        return bestMatch != null ? new DrawnShapeComponent(bestMatch.getId(), bestScore, bestMatch) : null;
    }

    private float[][] extractPoints(FloatArrayList points) {
        int count = points.size() / 2;
        float[][] result = new float[count][2];
        for (int i = 0; i < count; i++) {
            result[i][0] = points.getFloat(i * 2);
            result[i][1] = points.getFloat(i * 2 + 1);
        }
        return result;
    }

    public static float[][] preprocess(float[][] points) {
        float[][] resampled = resample(points, N);
        float[][] scaled = scaleTo(resampled);
        float[][] translated = translateTo(scaled);
        return canonicalizeStart(translated);
    }

    static boolean isClosed(float[][] points) {
        if (points.length < 2) return false;
        float dx = points[0][0] - points[points.length - 1][0];
        float dy = points[0][1] - points[points.length - 1][1];
        float closeDist = (float) Math.sqrt(dx * dx + dy * dy);
        float totalLen = pathLength(points);
        return closeDist < totalLen * 0.15f;
    }

    static float[][] canonicalizeStart(float[][] points) {
        if (!isClosed(points)) return points;

        float[] c = centroid(points);
        int bestIdx = 0;
        float bestAngleDist = Float.MAX_VALUE;
        for (int i = 0; i < points.length; i++) {
            float angle = Math.abs((float) Math.atan2(points[i][1] - c[1], points[i][0] - c[0]));
            if (angle < bestAngleDist) {
                bestAngleDist = angle;
                bestIdx = i;
            }
        }

        if (bestIdx == 0) return points;

        float[][] shifted = new float[points.length][2];
        for (int i = 0; i < points.length; i++) {
            int src = (i + bestIdx) % points.length;
            shifted[i][0] = points[src][0];
            shifted[i][1] = points[src][1];
        }
        return shifted;
    }

    static float[][] reverse(float[][] points) {
        float[][] result = new float[points.length][2];
        for (int i = 0; i < points.length; i++) {
            result[i][0] = points[points.length - 1 - i][0];
            result[i][1] = points[points.length - 1 - i][1];
        }
        return result;
    }

    private static float[][] resample(float[][] points, int n) {
        if (points.length < 2) {
            float[][] result = new float[n][2];
            float[] p = points.length > 0 ? points[0] : new float[2];
            for (int i = 0; i < n; i++) result[i] = new float[] { p[0], p[1] };
            return result;
        }

        float totalLen = pathLength(points);
        float interval = totalLen / (n - 1);

        List<float[]> newPoints = new ArrayList<>();
        newPoints.add(new float[] { points[0][0], points[0][1] });
        float accumulated = 0f;

        for (int i = 1; i < points.length; i++) {
            float dx = points[i][0] - points[i - 1][0];
            float dy = points[i][1] - points[i - 1][1];
            float segLen = (float) Math.sqrt(dx * dx + dy * dy);

            if (accumulated + segLen >= interval) {
                float t = (interval - accumulated) / segLen;
                float qx = points[i - 1][0] + t * dx;
                float qy = points[i - 1][1] + t * dy;
                newPoints.add(new float[] { qx, qy });

                float[][] remaining = new float[points.length - i + 1][2];
                remaining[0] = new float[] { qx, qy };
                System.arraycopy(points, i, remaining, 1, points.length - i);
                points = remaining;
                i = 0;
                accumulated = 0f;
            } else {
                accumulated += segLen;
            }
        }

        while (newPoints.size() < n) {
            float[] last = newPoints.getLast();
            newPoints.add(new float[] { last[0], last[1] });
        }

        return newPoints.subList(0, n).toArray(new float[0][]);
    }

    private float[][] rotateBy(float[][] points, float angle) {
        float[] c = centroid(points);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] result = new float[points.length][2];
        for (int i = 0; i < points.length; i++) {
            float dx = points[i][0] - c[0];
            float dy = points[i][1] - c[1];
            result[i][0] = dx * cos - dy * sin + c[0];
            result[i][1] = dx * sin + dy * cos + c[1];
        }
        return result;
    }

    private static float[][] scaleTo(float[][] points) {
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (float[] p : points) {
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
        }
        float w = maxX - minX;
        float h = maxY - minY;
        if (w < 1e-6f) w = 1e-6f;
        if (h < 1e-6f) h = 1e-6f;

        float[][] result = new float[points.length][2];
        float ratio = Math.min(w, h) / Math.max(w, h);
        if (ratio < 0.15f) {
            float scale = SQUARE_SIZE / Math.max(w, h);
            for (int i = 0; i < points.length; i++) {
                result[i][0] = points[i][0] * scale;
                result[i][1] = points[i][1] * scale;
            }
        } else {
            for (int i = 0; i < points.length; i++) {
                result[i][0] = points[i][0] * (SQUARE_SIZE / w);
                result[i][1] = points[i][1] * (SQUARE_SIZE / h);
            }
        }
        return result;
    }

    private static float[][] translateTo(float[][] points) {
        float[] c = centroid(points);
        float[][] result = new float[points.length][2];
        for (int i = 0; i < points.length; i++) {
            result[i][0] = points[i][0] - c[0];
            result[i][1] = points[i][1] - c[1];
        }
        return result;
    }

    private static float[] centroid(float[][] points) {
        float cx = 0, cy = 0;
        for (float[] p : points) { cx += p[0]; cy += p[1]; }
        return new float[] { cx / points.length, cy / points.length };
    }

    private static float pathLength(float[][] points) {
        float len = 0;
        for (int i = 1; i < points.length; i++) {
            float dx = points[i][0] - points[i - 1][0];
            float dy = points[i][1] - points[i - 1][1];
            len += (float) Math.sqrt(dx * dx + dy * dy);
        }
        return len;
    }

    private float distanceAtBestAngle(float[][] points, float[][] template,
            float a, float b, float threshold) {
        float x1 = PHI * a + (1 - PHI) * b;
        float f1 = distanceAtAngle(points, template, x1);
        float x2 = (1 - PHI) * a + PHI * b;
        float f2 = distanceAtAngle(points, template, x2);

        while (Math.abs(b - a) > threshold) {
            if (f1 < f2) {
                b = x2;
                x2 = x1;
                f2 = f1;
                x1 = PHI * a + (1 - PHI) * b;
                f1 = distanceAtAngle(points, template, x1);
            } else {
                a = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1 - PHI) * a + PHI * b;
                f2 = distanceAtAngle(points, template, x2);
            }
        }

        return Math.min(f1, f2);
    }

    private float distanceAtAngle(float[][] points, float[][] template, float angle) {
        float[][] rotated = rotateBy(points, angle);
        return pathDistance(rotated, template);
    }

    private float pathDistance(float[][] a, float[][] b) {
        float d = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            float dx = a[i][0] - b[i][0];
            float dy = a[i][1] - b[i][1];
            d += (float) Math.sqrt(dx * dx + dy * dy);
        }
        return d / len;
    }

    private float[][] extractOrderedBoundary(boolean[][] image) {
        List<float[]> boundary = new ArrayList<>();
        int h = image.length;
        int w = image[0].length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!image[y][x]) continue;
                boolean isBoundary = x == 0 || x == w - 1 || y == 0 || y == h - 1
                        || !image[y - 1][x] || !image[y + 1][x]
                        || !image[y][x - 1] || !image[y][x + 1];
                if (isBoundary) {
                    boundary.add(new float[] { x, y });
                }
            }
        }

        if (boundary.isEmpty()) return new float[0][2];

        float cx = 0, cy = 0;
        for (float[] p : boundary) { cx += p[0]; cy += p[1]; }
        cx /= boundary.size();
        cy /= boundary.size();

        final float fcx = cx, fcy = cy;
        boundary.sort((a, b) -> Float.compare(
                (float) Math.atan2(a[1] - fcy, a[0] - fcx),
                (float) Math.atan2(b[1] - fcy, b[0] - fcx)));

        return boundary.toArray(new float[0][]);
    }

    private float[][] extractMajorAxis(boolean[][] image) {
        int h = image.length;
        int w = image[0].length;
        List<int[]> filled = new ArrayList<>();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (image[y][x]) filled.add(new int[] { x, y });

        if (filled.size() < 2) return new float[][] { { 0, 0 }, { 1, 1 } };

        int[] best1 = filled.getFirst(), best2 = filled.getLast();
        float maxDist = 0;
        for (int i = 0; i < filled.size(); i++) {
            for (int j = i + 1; j < filled.size(); j++) {
                float dx = filled.get(j)[0] - filled.get(i)[0];
                float dy = filled.get(j)[1] - filled.get(i)[1];
                float dist = dx * dx + dy * dy;
                if (dist > maxDist) {
                    maxDist = dist;
                    best1 = filled.get(i);
                    best2 = filled.get(j);
                }
            }
        }

        return new float[][] {
                { best1[0], best1[1] },
                { best2[0], best2[1] }
        };
    }

    private void logTopCandidates(List<String> names, List<Float> scores) {
        List<int[]> indices = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) indices.add(new int[] { i });
        indices.sort((a, b) -> Float.compare(scores.get(b[0]), scores.get(a[0])));

        StringBuilder sb = new StringBuilder("[$1-Fixed]");
        for (int j = 0; j < Math.min(3, indices.size()); j++) {
            int idx = indices.get(j)[0];
            sb.append(String.format(" #%d: %s (%.4f)", j + 1, names.get(idx), scores.get(idx)));
            if (j < 2 && j < indices.size() - 1) sb.append(" |");
        }
        LOGGER.atFine().log(sb.toString());
    }

    @Override
    public String getName() {
        return "$1-Fixed";
    }

    @Override
    public void ensureLoaded() {
        if (initialized) return;
        initialized = true;

        // ShapeCacheManager.ensureLoaded();

        int[] counts = { 0, 0 };
        ShapeAsset.getAssetMap().getAssetMap().forEach((key, asset) -> {
            List<TemplateAsset> templates = TemplateAsset.getTemplatesForShape(key);
            if (!templates.isEmpty()) {
                templateCache.put(key, canonicalizeStart(templates.getFirst().getPointsAs2D()));
                counts[0]++;
                return;
            }
        });

        LOGGER.atInfo().log("DollarOneFixedDetector initialized with " + templateCache.size()
                + " templates (" + counts[0] + " from TemplateAsset, " + counts[1] + " from PNG).");
    }

    @Override
    public void clearCache() {
        templateCache.clear();
        initialized = false;
    }
}
