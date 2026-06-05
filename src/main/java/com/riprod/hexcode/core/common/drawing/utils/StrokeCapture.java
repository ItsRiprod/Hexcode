package com.riprod.hexcode.core.common.drawing.utils;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;
import com.riprod.hexcode.core.common.drawing.system.InterfaceManager;
import com.riprod.hexcode.core.common.drawing.system.shapes.DollarOneFixedDetector;
import com.riprod.hexcode.core.common.drawing.system.shapes.ShapeDetector;
import com.riprod.hexcode.utils.LatencyUtil;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

public final class StrokeCapture {
    private static final float MIN_RAD_DELTA = (float) Math.toRadians(0.5);
    private static final float MIN_RAD_DELTA_SQ = MIN_RAD_DELTA * MIN_RAD_DELTA;
    private static final float TWO_PI = (float) (2 * Math.PI);
    private static final ShapeDetector DEFAULT_DETECTOR = new DollarOneFixedDetector();

    private static final int PING_CREDIT_CAP_MS = 500;

    private StrokeCapture() {
    }

    public static boolean appendHeadSample(FloatArrayList points, HeadRotation head) {
        if (points == null || head == null) {
            return false;
        }
        float yaw = head.getRotation().y;
        float pitch = head.getRotation().x;

        if (!points.isEmpty()) {
            float lastYaw = points.getFloat(points.size() - 2);
            float lastPitch = points.getFloat(points.size() - 1);
            float dy = yaw - lastYaw;
            if (dy > (float) Math.PI) dy -= TWO_PI;
            else if (dy < -(float) Math.PI) dy += TWO_PI;
            yaw = lastYaw + dy;

            float dp = pitch - lastPitch;
            if (dy * dy + dp * dp < MIN_RAD_DELTA_SQ) {
                return false;
            }
        }

        points.add(yaw);
        points.add(pitch);
        return true;
    }

    @Nullable
    public static DrawnShapeComponent recognizeStroke(ComponentAccessor<EntityStore> accessor,
            Ref<EntityStore> playerRef, FloatArrayList points, ShapeDetector detector,
            long drawDurationMs) {
        if (points == null || points.size() < 3) {
            return null;
        }
        ShapeDetector det = detector != null ? detector : DEFAULT_DETECTOR;

        float minYaw = Float.MAX_VALUE, maxYaw = -Float.MAX_VALUE;
        float minPitch = Float.MAX_VALUE, maxPitch = -Float.MAX_VALUE;
        for (int i = 0; i < points.size(); i += 2) {
            float yaw = points.getFloat(i);
            float pitch = points.getFloat(i + 1);
            if (yaw < minYaw) minYaw = yaw;
            if (yaw > maxYaw) maxYaw = yaw;
            if (pitch < minPitch) minPitch = pitch;
            if (pitch > maxPitch) maxPitch = pitch;
        }

        DrawnShapeComponent result = det.detect(points, minYaw, maxYaw, minPitch, maxPitch);
        if (result == null) {
            return null;
        }

        result.setPoints(InterfaceManager.getPositionsFromAngles(accessor, points, playerRef, 4.0f));
        Color color = InterfaceManager.getColorFromQuality(result.getVolatility());
        result.setColor(color);

        int ping = LatencyUtil.pingMillis(accessor, playerRef);
        // server-driven feedback eats into effective draw time, so credit latency back
        result.setSpeed(Math.max(1L, drawDurationMs - Math.min(ping, PING_CREDIT_CAP_MS)));

        float maxSize = Math.max(Math.abs(maxYaw - minYaw), Math.abs(maxPitch - minPitch));
        result.setSize(maxSize);
        return result;
    }
}
