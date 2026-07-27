package com.riprod.hexcode.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
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
        if (var == null)
            return null;
        PositionVar pv = var.toPosition(accessor);
        if (pv == null || pv.getValue() == null)
            return null;
        return new Vector3d(pv.getValue());
    }

    @Nullable
    public static Rotation3f rotation(@Nullable HexVar var, @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null)
            return null;
        RotationVar rv = var.toRotation(accessor);
        return rv == null ? null : rv.getValue();
    }

    public static double positionAxis(@Nullable HexVar var, int axis,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null)
            return 0.0;
        if (var instanceof NumberVar nv) {
            return nv.getValue() == null ? 0.0 : nv.getValue();
        }
        Vector3d v = position(var, accessor);
        if (v == null)
            return 0.0;
        return switch (axis) {
            case 0 -> v.x;
            case 1 -> v.y;
            case 2 -> v.z;
            default -> 0.0;
        };
    }

    public static boolean isAbsolutePosition(@Nullable HexVar var,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null || var instanceof NumberVar)
            return false;
        PositionVar pv = var.toPosition(accessor);
        return pv != null && pv.isAbsolute();
    }

    public static double rotationAxis(@Nullable HexVar var, int axis,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null)
            return 0.0;
        if (var instanceof NumberVar nv) {
            return nv.getValue() == null ? 0.0 : nv.getValue();
        }
        Rotation3f v = rotation(var, accessor);
        if (v == null)
            return 0.0;
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
        if (var == null)
            return defaultValue;
        Double s = var.toScalar();
        return s == null ? defaultValue : s;
    }

    public static Double numberOrSlotDefault(@Nullable HexVar var, @Nullable SlotConfig slot) {
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
        return HexVarUtil.position(var, accessor);
    }

    @Nullable
    public static Vector3d resolveDirection(@Nullable HexVar var,
            @Nullable Vector3d sourcePosition,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        if (var == null)
            return null;

        if (var instanceof EntityVar entityVar) {
            Ref<EntityStore> entityRef = entityVar.getRef(accessor);
            if (entityRef == null || !entityRef.isValid())
                return null;
            try {
                HeadRotation headRot = accessor.getComponent(entityRef, HeadRotation.getComponentType());
                if (headRot != null)
                    return headRot.getDirection();
            } catch (Exception e) {
            }
            Rotation3f bodyRot = accessor.getComponent(entityRef, TransformComponent.getComponentType()).getRotation();
            return new RotationVar(bodyRot).forward();
        }
        if (var instanceof RotationVar rotVar && rotVar.getValue() != null) {
            return rotVar.forward();
        }
        if (var instanceof PositionVar posVar && posVar.getValue() != null) {
            if (posVar.isAbsolute()) {
                if (sourcePosition != null) {
                    Vector3d dir = new Vector3d(posVar.getValue()).sub(sourcePosition);
                    if (dir.length() > 1e-9)
                        return dir.normalize();
                }
                return null;
            }
            Vector3d offset = new Vector3d(posVar.getValue());
            if (offset.length() < 1e-9)
                return null;
            return offset.normalize();
        }
        if (var instanceof BlockVar blockVar) {
            Vector3i bv = blockVar.getValue();
            if (bv != null && sourcePosition != null) {
                Vector3d blockCenter = new Vector3d(bv.x + 0.5, bv.y + 0.5, bv.z + 0.5);
                Vector3d dir = blockCenter.sub(sourcePosition);
                if (dir.length() > 1e-9)
                    return dir.normalize();
            }
            return blockVar.toRotation(accessor).forward();
        }
        return null;
    }

    @Nullable
    public static Rotation3f resolveRotation(@Nullable HexVar var,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        Vector3d dir = resolveDirection(var, null, accessor);
        if (dir == null)
            return null;
        return Rotation3f.lookAt(dir, new Rotation3f());
    }
}
