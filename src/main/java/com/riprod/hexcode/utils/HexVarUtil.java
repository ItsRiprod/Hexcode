package com.riprod.hexcode.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.SlotAsset;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.NumberVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.RotationVar;

public class HexVarUtil {

    private HexVarUtil() {
    }

    @Nullable
    public static Vector3d position(@Nullable HexVar var, @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null) return null;
        PositionVar pv = var.toPosition(accessor);
        return pv == null ? null : pv.getValue();
    }

    @Nullable
    public static Rotation3f rotation(@Nullable HexVar var, @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null) return null;
        RotationVar rv = var.toRotation(accessor);
        return rv == null ? null : rv.getValue();
    }

    public static double positionAxis(@Nullable HexVar var, int axis,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null) return 0.0;
        if (var instanceof NumberVar nv) {
            return nv.getValue() == null ? 0.0 : nv.getValue();
        }
        Vector3d v = position(var, accessor);
        if (v == null) return 0.0;
        return switch (axis) {
            case 0 -> v.x;
            case 1 -> v.y;
            case 2 -> v.z;
            default -> 0.0;
        };
    }

    public static double rotationAxis(@Nullable HexVar var, int axis,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null) return 0.0;
        if (var instanceof NumberVar nv) {
            return nv.getValue() == null ? 0.0 : nv.getValue();
        }
        Rotation3f v = rotation(var, accessor);
        if (v == null) return 0.0;
        return switch (axis) {
            case 0 -> v.x;
            case 1 -> v.y;
            case 2 -> v.z;
            default -> 0.0;
        };
    }

    @Nullable
    public static Double number(@Nullable HexVar var) {
        return var instanceof NumberVar nv ? nv.getValue() : null;
    }

    public static Double numberOrDefault(@Nullable HexVar var, Double defaultValue) {
        if (var == null) return defaultValue;
        Double s = var.toScalar();
        return s == null ? defaultValue : s;
    }

    public static Double numberOrSlotDefault(@Nullable HexVar var, @Nullable SlotAsset slot) {
        Double fallback = slot != null ? slot.getDefaultValue() : null;
        return numberOrDefault(var, fallback != null ? fallback : 0.0);
    }

    @Nullable
    public static EntityVar resolveEntityVar(@Nullable HexVar var, @Nonnull HexContext ctx) {
        return var instanceof EntityVar ev ? ev : null;
    }

    @Nullable
    public static BlockVar resolveBlockVar(@Nullable HexVar var, @Nonnull HexContext ctx) {
        return var == null ? null : var.toBlockVar(ctx.getAccessor());
    }

    @Nullable
    public static PositionVar resolvePositionVar(@Nullable HexVar var, @Nonnull HexContext ctx) {
        return var == null ? null : var.toPosition(ctx.getAccessor());
    }

    @Nullable
    public static RotationVar resolveRotationVar(@Nullable HexVar var, @Nonnull HexContext ctx) {
        return var == null ? null : var.toRotation(ctx.getAccessor());
    }
}
