package com.riprod.hexcode.core.common.drawing.system.shapes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class CompositeShapeDetector implements ShapeDetector {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final List<ShapeDetector> detectors;
    private final ExecutorService executor;

    public CompositeShapeDetector() {
        this.detectors = new ArrayList<>();
        this.detectors.add(new RasterShapeDetector());
        this.detectors.add(new DirectProjectionDetector());
        this.detectors.add(new DollarOneDetector());
        this.detectors.add(new DollarOneFixedDetector());
        this.executor = Executors.newFixedThreadPool(detectors.size());
    }

    @Override
    public DrawnShapeComponent detect(FloatArrayList points, float minYaw, float maxYaw, float minPitch,
            float maxPitch) {
        List<CompletableFuture<TimedResult>> futures = new ArrayList<>();

        for (ShapeDetector detector : detectors) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    long startNanos = System.nanoTime();
                    DrawnShapeComponent result = detector.detect(points, minYaw, maxYaw, minPitch, maxPitch);
                    long elapsedNanos = System.nanoTime() - startNanos;
                    return new TimedResult(detector.getName(), result, elapsedNanos);
                } catch (Exception e) {
                    LOGGER.atSevere().log("[hexcode] shape detector %s failed: %s", detector.getName(), e.getMessage());
                    return new TimedResult(detector.getName(), null, 0);
                }
            }, executor));
        }

        List<TimedResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        StringBuilder sb = new StringBuilder("[Composite] timing:");
        for (TimedResult r : results) {
            sb.append(String.format(" %s: %.2fms", r.name, r.elapsedNanos / 1_000_000.0));
            if (r != results.getLast()) sb.append(" |");
        }
        LOGGER.atFine().log(sb.toString());

        StringBuilder resultSb = new StringBuilder("[Composite] results:");
        for (TimedResult r : results) {
            String shape = r.result != null ? r.result.getShapeId() : "none";
            float score = r.result != null ? r.result.getVolatility() : 0f;
            resultSb.append(String.format(" %s→%s (%.4f)", r.name, shape, score));
            if (r != results.getLast()) resultSb.append(" |");
        }
        LOGGER.atFine().log(resultSb.toString());

        if (results.isEmpty() || results.getFirst().result == null) {
            return null;
        }
        return results.getFirst().result;
    }

    @Override
    public String getName() {
        return "Composite";
    }

    @Override
    public void ensureLoaded() {
        for (ShapeDetector detector : detectors) {
            detector.ensureLoaded();
        }
    }

    @Override
    public void clearCache() {
        for (ShapeDetector detector : detectors) {
            detector.clearCache();
        }
    }

    private record TimedResult(String name, DrawnShapeComponent result, long elapsedNanos) {
    }
}
