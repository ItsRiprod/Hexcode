package com.riprod.hexcode.core.common.utilities;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class OrientedDebugUtil {

    private static final double EPSILON = 0.001;

    private OrientedDebugUtil() {
    }

    public static void addCone(ComponentAccessor<EntityStore> accessor, Vector3d from, Vector3d to,
            Vector3f color, double diameter, float time) {
        Vector3d direction = new Vector3d(to.x - from.x, to.y - from.y, to.z - from.z);
        double length = direction.length();
        if (length < EPSILON) return;

        Matrix4d matrix = buildOrientedMatrix(from, direction, length, diameter);
        DebugEmitter.add(accessor, DebugShape.Cone, matrix, color, DebugUtils.DEFAULT_OPACITY, time,
                DebugUtils.FLAG_NO_WIREFRAME);
    }

    public static void addCylinder(ComponentAccessor<EntityStore> accessor, Vector3d from, Vector3d to,
            Vector3f color, double diameter, float time, int flags, float opacity) {
        if (opacity <= 0f) return;
        Vector3d direction = new Vector3d(to.x - from.x, to.y - from.y, to.z - from.z);
        double length = direction.length();
        if (length < EPSILON) return;

        Matrix4d matrix = buildOrientedMatrix(from, direction, length, diameter);
        DebugEmitter.add(accessor, DebugShape.Cylinder, matrix, color, opacity, time,
                flags | DebugUtils.FLAG_NO_WIREFRAME);
    }

    private static Matrix4d buildOrientedMatrix(Vector3d origin, Vector3d direction,
            double length, double diameter) {
        Matrix4d matrix = new Matrix4d();
        matrix.identity();
        matrix.translate(origin);

        double angleY = Math.atan2(direction.z, direction.x);
        matrix.rotate(-(angleY + (Math.PI / 2)), 0.0, 1.0, 0.0);

        double xzLen = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        double angleX = Math.atan2(xzLen, direction.y);
        matrix.rotate(-angleX, 1.0, 0.0, 0.0);

        // cone is center-anchored; shift base to origin then scale
        matrix.translate(0.0, length / 2.0, 0.0);
        matrix.scale(diameter, length, diameter);
        return matrix;
    }
}
