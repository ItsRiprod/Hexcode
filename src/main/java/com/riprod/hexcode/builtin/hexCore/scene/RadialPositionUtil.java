package com.riprod.hexcode.builtin.hexCore.scene;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3f;

public class RadialPositionUtil {
    public static List<Vector3f> calculateOffsets(int count, float radius, float angleOffset, @Nullable Vector3f centerOffset) {
        List<Vector3f> positions = new ArrayList<>();

        if (count <= 0) {
            return positions;
        }

        float angleStep = (float) (2 * Math.PI / count);

        for (int i = 0; i < count; i++) {
            float yaw = angleStep * i + angleOffset;
            float x = (float) Math.cos(yaw) * radius;
            float z = (float) Math.sin(yaw) * radius;
            Vector3f offset = new Vector3f(x, 0, z);
            if (centerOffset != null) {
                offset.add(centerOffset);
            }
            positions.add(offset);
        }

        return positions;
    }
}
