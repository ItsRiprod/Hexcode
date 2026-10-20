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
import com.riprod.hexcode.core.common.execution.context.HexContext;
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
        if (var == null) return new Vector3d();
        return var.position(accessor);
    }
}
