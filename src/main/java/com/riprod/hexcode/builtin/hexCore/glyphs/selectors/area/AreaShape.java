package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import org.joml.Vector3d;

import com.hypixel.hytale.protocol.DebugShape;

public enum AreaShape {
    CUBE,
    ELLIPSOID,
    CYLINDER;

    public static AreaShape fromSlotValue(double value) {
        if (value < -0.5)
            return CUBE;
        if (value > 0.5)
            return CYLINDER;
        return ELLIPSOID;
    }

    public DebugShape debugShape() {
        return switch (this) {
            case CUBE -> DebugShape.Cube;
            case ELLIPSOID -> DebugShape.Sphere;
            case CYLINDER -> DebugShape.Cylinder;
        };
    }

    public double volume(Vector3d half) {
        if (half.x <= 0 || half.y <= 0 || half.z <= 0)
            return 0.0;
        return switch (this) {
            case CUBE -> 8.0 * half.x * half.y * half.z;
            case ELLIPSOID -> (4.0 / 3.0) * Math.PI * half.x * half.y * half.z;
            case CYLINDER -> 2.0 * Math.PI * half.x * half.y * half.z;
        };
    }

    public boolean contains(Vector3d half, double dx, double dy, double dz) {
        if (half.x <= 0 || half.y <= 0 || half.z <= 0)
            return false;
        return switch (this) {
            case CUBE -> Math.abs(dx) <= half.x && Math.abs(dy) <= half.y && Math.abs(dz) <= half.z;
            case ELLIPSOID -> {
                double nx = dx / half.x;
                double ny = dy / half.y;
                double nz = dz / half.z;
                yield nx * nx + ny * ny + nz * nz <= 1.0;
            }
            case CYLINDER -> {
                if (Math.abs(dy) > half.y)
                    yield false;
                double nx = dx / half.x;
                double nz = dz / half.z;
                yield nx * nx + nz * nz <= 1.0;
            }
        };
    }

    public double halfWidthX(Vector3d half, double dy, double dz) {
        if (half.x <= 0 || half.y <= 0 || half.z <= 0)
            return -1.0;
        return switch (this) {
            case CUBE -> Math.abs(dy) <= half.y && Math.abs(dz) <= half.z ? half.x : -1.0;
            case ELLIPSOID -> {
                double ny = dy / half.y;
                double nz = dz / half.z;
                double remaining = 1.0 - ny * ny - nz * nz;
                yield remaining < 0 ? -1.0 : half.x * Math.sqrt(remaining);
            }
            case CYLINDER -> {
                if (Math.abs(dy) > half.y)
                    yield -1.0;
                double nz = dz / half.z;
                double remaining = 1.0 - nz * nz;
                yield remaining < 0 ? -1.0 : half.x * Math.sqrt(remaining);
            }
        };
    }
}
