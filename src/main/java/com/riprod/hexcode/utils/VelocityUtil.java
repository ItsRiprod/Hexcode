package com.riprod.hexcode.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class VelocityUtil {

    public static boolean isProjectile(Ref<EntityStore> ref, ComponentAccessor<EntityStore> buffer) {
        return buffer.getComponent(ref, StandardPhysicsProvider.getComponentType()) != null;
    }

    public static boolean isPhysicsTicked(Ref<EntityStore> ref, ComponentAccessor<EntityStore> buffer) {
        return buffer.getComponent(ref, StandardPhysicsProvider.getComponentType()) != null
                && buffer.getComponent(ref, HeadRotation.getComponentType()) != null;
    }

    public static Vector3d currentVelocity(Ref<EntityStore> ref, ComponentAccessor<EntityStore> buffer) {
        Velocity vel = buffer.getComponent(ref, Velocity.getComponentType());
        if (vel == null) return new Vector3d();
        boolean player = buffer.getComponent(ref, Player.getComponentType()) != null;
        Vector3d source = player ? vel.getClientVelocity() : vel.getVelocity();
        return source == null ? new Vector3d() : new Vector3d(source);
    }

    public static void applyVelocity(Ref<EntityStore> ref, Vector3d velocity,
            ChangeVelocityType type, VelocityConfig config,
            CommandBuffer<EntityStore> buffer) {
        if (type == ChangeVelocityType.Set && isPhysicsTicked(ref, buffer)) {
            StandardPhysicsProvider physics = buffer.getComponent(ref,
                    StandardPhysicsProvider.getComponentType());
            physics.getForceProviderStandardState().nextTickVelocity.set(velocity);
            if (physics.getState() != StandardPhysicsProvider.STATE.ACTIVE) {
                physics.setState(StandardPhysicsProvider.STATE.ACTIVE);
            }
            return;
        }

        Velocity vel = buffer.getComponent(ref, Velocity.getComponentType());
        if (vel != null) {
            vel.addInstruction(new Vector3d(velocity), config, type);
        }
    }
}
