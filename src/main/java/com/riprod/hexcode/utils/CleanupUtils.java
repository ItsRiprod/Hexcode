package com.riprod.hexcode.utils;

import com.hypixel.hytale.builtin.mounts.MountedByComponent;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CleanupUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void safeRemoveEntity(CommandBuffer<EntityStore> buffer, Ref<EntityStore> entityRef) {
        if (entityRef != null && entityRef.isValid()) {
            try {
                buffer.tryRemoveEntity(entityRef, RemoveReason.REMOVE);
            } catch (Exception e) {
                LOGGER.atWarning().log("Error occurred while removing entity", e);
            }
        }
    }

    public static void safeRemoveEntities(CommandBuffer<EntityStore> buffer, Iterable<Ref<EntityStore>> entityRefs) {
        for (Ref<EntityStore> ref : entityRefs) {
            safeRemoveEntity(buffer, ref);
        }
    }

    public static void safeRemoveMountParent(CommandBuffer<EntityStore> buffer, Ref<EntityStore> parentRef) {
        if (parentRef == null || !parentRef.isValid()) return;
        try {
            unmountPassengers(buffer, parentRef);
            buffer.tryRemoveEntity(parentRef, RemoveReason.REMOVE);
        } catch (Exception e) {
            LOGGER.atWarning().log("Error occurred while removing mount parent", e);
        }
    }

    public static void safeRemoveConstruct(CommandBuffer<EntityStore> buffer, Ref<EntityStore> constructRef) {
        if (constructRef == null || !constructRef.isValid()) return;
        try {
            MountedByComponent ridden = buffer.getComponent(constructRef, MountedByComponent.getComponentType());
            if (ridden != null) {
                for (Ref<EntityStore> passenger : ridden.getPassengers()) {
                    safeRemoveEntity(buffer, passenger);
                }
            }
            unmountPassengers(buffer, constructRef);
            buffer.tryRemoveEntity(constructRef, RemoveReason.REMOVE);
        } catch (Exception e) {
            LOGGER.atWarning().log("Error occurred while removing construct", e);
        }
    }

    private static void unmountPassengers(CommandBuffer<EntityStore> buffer, Ref<EntityStore> parentRef) {
        MountedByComponent ridden = buffer.getComponent(parentRef, MountedByComponent.getComponentType());
        if (ridden == null) return;
        for (Ref<EntityStore> passenger : ridden.getPassengers()) {
            if (passenger == null || !passenger.isValid()) continue;
            buffer.tryRemoveComponent(passenger, MountedComponent.getComponentType());
        }
    }
}
