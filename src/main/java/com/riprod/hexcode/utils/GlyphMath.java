package com.riprod.hexcode.utils;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.math.vector.Rotation3f;

public class GlyphMath {

    private GlyphMath() {
    }

    public static Vector3d sphericalToCartesian(Vector3d origin, float yaw, float pitch, float distance) {
        double x = origin.x + distance * (Math.cos(pitch) * -Math.sin(yaw));
        double y = origin.y + distance * Math.sin(pitch);
        double z = origin.z + distance * (Math.cos(pitch) * -Math.cos(yaw));
        return new Vector3d(x, y, z);
    }

    public static Vector3d sphericalToCartesian(Rotation3f pos) {
        return sphericalToCartesian(new Vector3d(0, 0, 0), pos.y, pos.x, pos.z);
    }

    public static Vector3d sphericalToCartesian(Vector3d origin, Rotation3f pos) {
        return sphericalToCartesian(origin, pos.y, pos.x, pos.z());
    }

    public static boolean isPointInGlyphArea(Vector3f glyphPos, Vector3f lookPos, float scale) {
        float angularDistance = calculateAngularDistance(glyphPos, lookPos);
        float selectionRadius = getSelectionRadius(scale);
        return angularDistance <= selectionRadius;
    }

    public static float calculateAngularDistance(Vector3f a, Vector3f b) {
        return calculateAngularDistance(a.x, a.y, b.x, b.y);
    }

    public static float calculateAngularDistance(float pitchA, float yawA, float pitchB, float yawB) {
        double cosAngle = Math.sin(pitchA) * Math.sin(pitchB)
                + Math.cos(pitchA) * Math.cos(pitchB) * Math.cos(yawA - yawB);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle));
        return (float) Math.acos(cosAngle);
    }

    public static float getSelectionRadius(float scale) {
        float baseRadius = 0.12f;
        return baseRadius * scale;
    }

    public static List<Rotation3f> getChildRotations(int childrenCount,
            float parentScale, float distance) {
        if (childrenCount <= 0) {
            return null;
        }

        if (childrenCount == 1) {
            return List.of(new Rotation3f(0, 0, 0));
        }

        float angleIncrement = (float) (2 * Math.PI / childrenCount);
        float angularRadius = getSelectionRadius(parentScale) * 1.2f; // scales with the parent scale

        List<Rotation3f> childAngles = new ArrayList<>();
        for (int i = 0; i < childrenCount; i++) {
            float theta = i * angleIncrement;
            Rotation3f childPos = new Rotation3f(
                    angularRadius * (float) Math.cos(theta),
                    angularRadius * (float) Math.sin(theta),
                    distance);
            childAngles.add(childPos);
        }
        return childAngles;
    }

    public static Vector3f toMountOffset(Rotation3f childRotation, Rotation3f parentRotation) {
        float dpitch = childRotation.x;
        float dyaw = childRotation.y;
        return new Vector3f(
                -dyaw * parentRotation.z(),
                dpitch * parentRotation.z(),
                0);
    }
}
