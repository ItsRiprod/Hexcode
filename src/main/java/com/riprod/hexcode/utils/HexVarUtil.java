package com.riprod.hexcode.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;
import com.riprod.hexcode.core.common.glyphs.variables.VarType;

public class HexVarUtil {

    private HexVarUtil() {
    }

    public static Double numberOrDefault(@Nullable HexVar var, Double defaultValue) {
        if (var == null)
            return defaultValue;
        Double s = var.scalar();
        return s == null || !Double.isFinite(s) ? defaultValue : s;
    }

    public static Double numberOrSlotDefault(@Nullable HexVar var, @Nullable SlotConfig slot) {
        Double fallback = slot != null ? slot.getDefaultValue() : null;
        return numberOrDefault(var, fallback != null ? fallback : 0.0);
    }

    @Nullable
    public static Vector3d resolveEyePosition(@Nullable HexVar var,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var instanceof EntityVar entityVar) {
            var entityRef = entityVar.getRef(accessor);
            if (entityRef != null && entityRef.isValid()) {
                HeadRotation headRot = accessor.getComponent(entityRef, HeadRotation.getComponentType());
                if (headRot != null) {
                    return TargetUtil.getLook(entityRef, accessor).getPosition();
                }
                TransformComponent tc = accessor.getComponent(entityRef, TransformComponent.getComponentType());
                if (tc != null)
                    return new Vector3d(tc.getPosition());
            }
        }
        if (var == null)
            return new Vector3d();
        return var.position(accessor);
    }

    public static boolean isPositionType(HexVar var) {
        return var.type() == VarType.POSITION || var.type() == VarType.TRANSFORM;
    }

    public static boolean isRotationType(HexVar var) {
        return var.type() == VarType.ROTATION || var.type() == VarType.TRANSFORM;
    }

    public static double axis(Rotation3f pos, int axis) {
        return switch (axis) {
            case 0 -> pos.x();
            case 1 -> pos.y();
            case 2 -> pos.z();
            default -> 0;
        };
    }

    public static double axis(Vector3d pos, int axis) {
        return switch (axis) {
            case 0 -> pos.x;
            case 1 -> pos.y;
            case 2 -> pos.z;
            default -> 0;
        };
    }
}
