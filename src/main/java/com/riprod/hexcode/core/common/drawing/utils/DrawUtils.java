package com.riprod.hexcode.core.common.drawing.utils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.dispatch.ShapeStructure;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.system.GlyphCreationManager;
import com.riprod.hexcode.core.common.drawing.system.InterfaceManager;
import com.riprod.hexcode.core.common.drawing.system.ShapeTemplateStore;
import com.riprod.hexcode.core.common.drawing.system.shapes.DollarOneFixedDetector;
import com.riprod.hexcode.core.common.drawing.system.shapes.ShapeDetector;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.utils.LatencyUtil;
import com.riprod.hexcode.utils.LogScopes;

public final class DrawUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.get(LogScopes.ASSETS);

    private static final float FINALIZE_BASE_SECONDS = 0.65f;
    private static final float FINALIZE_PING_FACTOR = 2.0f;

    private static ShapeDetector shapeDetector = new DollarOneFixedDetector();

    private DrawUtils() {
    }

    public static ShapeDetector getShapeDetector() {
        return shapeDetector;
    }

    public static void setShapeDetector(ShapeDetector detector) {
        shapeDetector = detector;
    }

    public static void requestTraining(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> player, String shapeId,
            @Nullable String packOverride) {
        var hexcasterComponent = accessor.getComponent(player, HexcasterComponent.getComponentType());
        hexcasterComponent.setTrainingPackOverride(packOverride);
        hexcasterComponent.setTrainingShapeId(shapeId);
    }

    public static void beginStroke(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            DrawCaptureComponent capture, HeadRotation head) {
        Ref<EntityStore> oldTrail = capture.getDrawTrailRef();
        if (oldTrail != null && oldTrail.isValid()) {
            InterfaceManager.removeTrailEntity(buffer, oldTrail);
        }

        capture.getStrokePoints().clear();
        capture.setStrokeStartMillis(nowMillis(buffer));
        capture.setStrokeActive(true);
        capture.setFinalizePending(false);
        capture.setFinalizeTimer(0f);
        capture.setDrawTrailRef(InterfaceManager.spawnTrailEntity(buffer, player, head));
    }

    public static void tickStroke(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            DrawCaptureComponent capture, HeadRotation head) {
        if (StrokeCapture.appendHeadSample(capture.getStrokePoints(), head)) {
            InterfaceManager.positionTrailEntity(buffer, player, capture.getDrawTrailRef(), head);
        }
    }

    public static void endStroke(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            DrawCaptureComponent capture) {
        Ref<EntityStore> trailRef = capture.getDrawTrailRef();
        if (trailRef != null) {
            capture.getPersistentStrokeRefs().add(trailRef);
            capture.setDrawTrailRef(null);
        }
        capture.setStrokeActive(false);

        if (saveToOverride(buffer, player, capture)) return;

        long durationMs = nowMillis(buffer) - capture.getStrokeStartMillis();
        DrawnShapeComponent shape = StrokeCapture.recognizeStroke(buffer, player,
                capture.getStrokePoints(), shapeDetector, durationMs);
        capture.getStrokePoints().clear();

        if (shape != null) {
            capture.getPendingShapes().add(shape);
        }
        if (!capture.getPendingShapes().isEmpty()) {
            openFinalizeWindow(buffer, player, capture);
        }
    }

    private static boolean saveToOverride(ComponentAccessor<EntityStore> accessor, Ref<EntityStore> player, DrawCaptureComponent capture) {
        var hexcasterComponent = accessor.getComponent(player, HexcasterComponent.getComponentType());
        if (hexcasterComponent == null) return false;
        var packOverride = hexcasterComponent.getTrainingPackOverride();
        var shapeId = hexcasterComponent.getTrainingShapeId();

        if (packOverride == null || shapeId == null) return false;
        saveTrainingTemplate(packOverride, shapeId, capture);
        capture.getStrokePoints().clear();

        hexcasterComponent.setTrainingPackOverride(null);
        hexcasterComponent.setTrainingShapeId(null);
        return true;
    }

    public static void openFinalizeWindow(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            DrawCaptureComponent capture) {
        capture.setFinalizeTimer(0f);
        capture.setFinalizePending(true);
        if (capture.getFinalizeDelaySeconds() < 0f) {
            return;
        }
        float pingSeconds = LatencyUtil.pingMillis(buffer, player) / 1000f;
        capture.setFinalizeDelaySeconds(FINALIZE_BASE_SECONDS + pingSeconds * FINALIZE_PING_FACTOR);
    }

    @Nullable
    public static ShapeStructure computeStructure(List<DrawnShapeComponent> shapes) {
        if (shapes == null || shapes.isEmpty()) {
            return null;
        }
        GlyphCreationManager.NormalizeShapeSizes(shapes);
        float efficiency = ShapeComparator.calculateEfficiency(shapes);
        float volatility = ShapeComparator.calculateVolatility(shapes);
        return new ShapeStructure(List.copyOf(shapes), efficiency, volatility);
    }

    private static void saveTrainingTemplate(String packOverride, String shapeId, DrawCaptureComponent capture) {
        ShapeTemplateStore.Result result = ShapeTemplateStore.saveTemplate(shapeId,
                capture.getStrokePoints(), packOverride);
        if (result.success) {
            shapeDetector.clearCache();
            LOGGER.atFine().log("recorded training template for '%s' (%d points) into pack '%s'",
                    shapeId, capture.getStrokePoints().size() / 2, result.packName);
        } else {
            LOGGER.atWarning().log("training template for '%s' failed: %s", shapeId, result.error);
        }
    }

    private static long nowMillis(CommandBuffer<EntityStore> buffer) {
        return buffer.getResource(TimeResource.getResourceType()).getNow().toEpochMilli();
    }
}
