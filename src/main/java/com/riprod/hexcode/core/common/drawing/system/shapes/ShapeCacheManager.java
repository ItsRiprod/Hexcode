package com.riprod.hexcode.core.common.drawing.system.shapes;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;

public class ShapeCacheManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final int NUM_RAYS = 96;
    private static final Map<String, boolean[][]> imageData = new ConcurrentHashMap<>();
    private static final Map<String, int[][]> distanceTransformData = new ConcurrentHashMap<>();
    private static final Map<String, float[]> radialSignatureData = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    public static void ensureLoaded() {
        if (initialized) {
            return;
        }
        initialized = true;

        ShapeAsset.getAssetMap().getAssetMap().forEach((key, asset) -> {
            try {
                String imageId = /* asset.getImagePath(); */ key; // Use key as imageId since ImagePath is removed
                if (!imageData.containsKey(key)) {
                    boolean[][] imgData = importImageData(key, imageId);
                    int[][] distData = computeDistanceTransform(imgData);

                    imageData.put(key, imgData);
                    distanceTransformData.put(key, distData);

                    if (!Boolean.TRUE.equals(asset.getCenterFilled())) {
                        float[] radialSig = computeRadialSignature(imgData);
                        radialSignatureData.put(key, radialSig);
                    }
                }
            } catch (Exception e) {
                LOGGER.atSevere().withCause(e).log("Failed to load shape asset: " + key);
            }
        });

        LOGGER.atInfo().log("ShapeCacheManager initialized with " + imageData.size() + " assets.");
    }

    private static boolean[][] importImageData(String key, String imageId) {
        Path jsonPath = ShapeAsset.getAssetMap().getPath(key);
        Path pngPath = jsonPath.resolveSibling(imageId + ".png");
        try (InputStream stream = Files.newInputStream(pngPath)) {

            BufferedImage image = ImageIO.read(stream);
            int w = image.getWidth();
            int h = image.getHeight();
            boolean[][] grid = new boolean[h][w];

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int brightness = (r + g + b) / 3;
                    grid[y][x] = alpha > 128 && brightness > 128;
                }
            }
            return grid;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load shape image: " + jsonPath, e);
        }
    }

    public static float[] computeRadialSignature(boolean[][] grid) {
        float[] signature = new float[NUM_RAYS];
        int h = grid.length;
        int w = grid[0].length;
        float cx = w / 2f;
        float cy = h / 2f;
        float maxPossibleDist = (float) Math.sqrt(cx * cx + cy * cy);

        for (int i = 0; i < NUM_RAYS; i++) {
            double angle = 2.0 * Math.PI * i / NUM_RAYS;
            float dx = (float) Math.cos(angle);
            float dy = (float) Math.sin(angle);

            float maxDist = 0f;
            for (float t = 0f; ; t += 0.5f) {
                int px = (int) (cx + dx * t);
                int py = (int) (cy + dy * t);
                if (px < 0 || px >= w || py < 0 || py >= h) break;
                if (grid[py][px]) {
                    maxDist = t;
                }
            }
            signature[i] = maxDist / maxPossibleDist;
        }

        return signature;
    }

    public static int[][] computeDistanceTransform(boolean[][] grid) {
        int h = grid.length;
        int w = grid[0].length;
        int[][] dist = new int[h][w];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (grid[y][x]) {
                    dist[y][x] = 0;
                    queue.add(new int[] { x, y });
                } else {
                    dist[y][x] = Integer.MAX_VALUE;
                }
            }
        }

        // 8-connected neighbors (chebyshev: diagonal = 1)
        int[][] dirs = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int nd = dist[cell[1]][cell[0]] + 1;
            for (int[] d : dirs) {
                int nx = cell[0] + d[0], ny = cell[1] + d[1];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && dist[ny][nx] > nd) {
                    dist[ny][nx] = nd;
                    queue.add(new int[] { nx, ny });
                }
            }
        }

        return dist;
    }

    public static boolean[][] getImageData(String key) {
        ensureLoaded();
        boolean[][] data = imageData.get(key);
        if (data != null) {
            return data;
        }
        // attempt to load missing data
        ShapeAsset asset = ShapeAsset.getAssetMap().getAsset(key);
        if (asset != null) {
            data = importImageData(key, key);
            imageData.put(key, data);
        } else {
            LOGGER.atSevere().log("Shape asset not found for key: " + key);
            return null;
        }

        return imageData.get(key);
    }

    public static int[][] getDistanceTransform(String key) {
        ensureLoaded();
        int[][] data = distanceTransformData.get(key);
        if (data != null) {
            return data;
        }
        // attempt to compute missing data
        boolean[][] imgData = getImageData(key);
        if (imgData != null) {
            data = computeDistanceTransform(imgData);
            distanceTransformData.put(key, data);
        } else {
            LOGGER.atSevere().log("Cannot compute distance transform, image data missing for key: " + key);
            return null;
        }

        return distanceTransformData.get(key);
    }

    public static float[] getRadialSignature(String key) {
        ensureLoaded();
        float[] data = radialSignatureData.get(key);
        if (data != null) {
            return data;
        }
        boolean[][] imgData = getImageData(key);
        if (imgData != null) {
            data = computeRadialSignature(imgData);
            radialSignatureData.put(key, data);
        } else {
            LOGGER.atSevere().log("Cannot compute radial signature, image data missing for key: " + key);
            return null;
        }
        return radialSignatureData.get(key);
    }

    public static boolean hasImageData(String key) {
        ensureLoaded();
        return imageData.containsKey(key);
    }

    public static void clearCache() {
        imageData.clear();
        distanceTransformData.clear();
        radialSignatureData.clear();
        initialized = false;
    }
}
