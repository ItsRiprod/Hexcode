package com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate;

import java.lang.reflect.Field;
import java.util.UUID;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class GlaciatePhysicsConfig extends StandardPhysicsConfig {

    public static final GlaciatePhysicsConfig INSTANCE = new GlaciatePhysicsConfig();

    private GlaciatePhysicsConfig() {
        this.gravity = 20;
        this.bounceCount = -1;
        this.bounciness = 0.3;
        this.sticksVertically = false;
        this.computeYaw = false;
        this.computePitch = false;
        this.terminalVelocityAir = 50.0;
        this.terminalVelocityWater = 10.0;
    }

    @Override
    public void apply(Holder<EntityStore> holder, Ref<EntityStore> creatorRef,
            Vector3d velocity, ComponentAccessor<EntityStore> accessor,
            boolean predicted) {
        UUID creatorUUID = null;
        if (creatorRef != null) {
            UUIDComponent uuidComponent = accessor.getComponent(
                    creatorRef, UUIDComponent.getComponentType());
            if (uuidComponent != null) creatorUUID = uuidComponent.getUuid();
        }
        BoundingBox boundingBox = holder.getComponent(BoundingBox.getComponentType());
        StandardPhysicsProvider provider = new StandardPhysicsProvider(
                boundingBox, creatorUUID, this, velocity, predicted);
        try {
            Field field = StandardPhysicsProvider.class.getDeclaredField("provideCharacterCollisions");
            field.setAccessible(true);
            field.setBoolean(provider, false);
        } catch (Exception ignored) {
        }
        holder.addComponent(StandardPhysicsProvider.getComponentType(), provider);
    }
}
