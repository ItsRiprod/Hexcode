package com.riprod.hexcode.core.common.drawing.system.shapes;

import java.util.ArrayList;
import java.util.List;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;
import com.riprod.hexcode.core.common.drawing.utils.DrawRasterizer;
import com.riprod.hexcode.core.common.drawing.utils.ShapeComparator;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class RasterShapeDetector implements ShapeDetector {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final int GRID_SIZE = 32;

    @Override
    public DrawnShapeComponent detect(FloatArrayList points, float minYaw, float maxYaw, float minPitch,
            float maxPitch) {
        boolean[][] grid = DrawRasterizer.rasterize(points, GRID_SIZE, minYaw, maxYaw, minPitch, maxPitch);

        int[][] drawnDT = ShapeCacheManager.computeDistanceTransform(grid);
        float[] drawnRadialSig = ShapeCacheManager.computeRadialSignature(grid);

        List<float[]> scored = new ArrayList<>();
        float bestAccuracy = -1f;
        ShapeAsset bestMatch = null;

        for (var entry : ShapeAsset.getAssetMap().getAssetMap().entrySet()) {
            ShapeAsset asset = entry.getValue();
            float accuracy = ShapeComparator.compareShapes(grid, drawnDT, drawnRadialSig, asset);
            scored.add(new float[] { accuracy });
            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestMatch = asset;
            }
        }

        logTopCandidates(scored);

        return bestMatch != null ? new DrawnShapeComponent(bestMatch.getId(), bestAccuracy, bestMatch) : null;
    }

    private void logTopCandidates(List<float[]> scored) {
        List<String[]> candidates = new ArrayList<>();
        int i = 0;
        for (var entry : ShapeAsset.getAssetMap().getAssetMap().entrySet()) {
            if (i < scored.size()) {
                candidates.add(new String[] { entry.getKey(), String.valueOf(scored.get(i)[0]) });
            }
            i++;
        }
        candidates.sort((a, b) -> Float.compare(Float.parseFloat(b[1]), Float.parseFloat(a[1])));

        StringBuilder sb = new StringBuilder("[Raster] top 3:");
        for (int j = 0; j < Math.min(3, candidates.size()); j++) {
            sb.append(String.format(" #%d: %s (%.4f)", j + 1, candidates.get(j)[0],
                    Float.parseFloat(candidates.get(j)[1])));
            if (j < 2 && j < candidates.size() - 1) sb.append(" |");
        }
        LOGGER.atInfo().log(sb.toString());
    }

    @Override
    public String getName() {
        return "Raster";
    }

    @Override
    public void ensureLoaded() {
        ShapeCacheManager.ensureLoaded();
    }

    @Override
    public void clearCache() {
        ShapeCacheManager.clearCache();
    }
}
