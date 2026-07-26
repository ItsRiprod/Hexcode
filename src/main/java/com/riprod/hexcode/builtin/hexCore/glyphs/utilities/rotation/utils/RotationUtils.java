package com.riprod.hexcode.builtin.hexCore.glyphs.utilities.rotation.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class RotationUtils {
    private RotationUtils() {
    }

    public static final float EPSILON_RADIANS = (float) Math.toRadians(1.0);

    private static final double TWO_PI = Math.PI * 2.0;

    public static boolean applyExact(Ref<EntityStore> ref, Rotation3f rotation,
            CommandBuffer<EntityStore> accessor) {
        TransformComponent tc = accessor.getComponent(ref, TransformComponent.getComponentType());
        if (tc == null) return false;

        HeadRotation hr = accessor.getComponent(ref, HeadRotation.getComponentType());
        if (hr == null) {
            tc.setRotation(rotation);
            return true;
        }

        if (withinEpsilon(hr.getRotation(), rotation)) return false;
        if (teleportInFlight(ref, accessor)) return false;

        Rotation3f headRotation = new Rotation3f(rotation);
        Rotation3f bodyRotation = new Rotation3f(0.0f, headRotation.yaw(), 0.0f);
        accessor.putComponent(ref, Teleport.getComponentType(),
                Teleport.createExact(tc.getPosition(), bodyRotation, headRotation)
                        .withoutVelocityReset());
        return true;
    }

    public static boolean teleportInFlight(Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {
        return accessor.getComponent(ref, PendingTeleport.getComponentType()) != null
                || accessor.getComponent(ref, Teleport.getComponentType()) != null;
    }

    public static boolean withinEpsilon(Rotation3f current, Rotation3f target) {
        return axisWithinEpsilon(current.pitch(), target.pitch())
                && axisWithinEpsilon(current.yaw(), target.yaw())
                && axisWithinEpsilon(current.roll(), target.roll());
    }

    private static boolean axisWithinEpsilon(float current, float target) {
        return Math.abs(wrapToPi(current - target)) <= EPSILON_RADIANS;
    }

    private static float wrapToPi(float radians) {
        return (float) Math.IEEEremainder(radians, TWO_PI);
    }
}
