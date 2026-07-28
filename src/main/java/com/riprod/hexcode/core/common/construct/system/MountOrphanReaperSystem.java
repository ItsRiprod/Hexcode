package com.riprod.hexcode.core.common.construct.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.utils.CleanupUtils;

public class MountOrphanReaperSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return MountedComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            MountedComponent mounted = chunk.getComponent(index, MountedComponent.getComponentType());
            if (mounted == null) return;

            Ref<EntityStore> parentRef = mounted.getMountedToEntity();
            if (parentRef == null || parentRef.isValid()) return;

            Ref<EntityStore> selfRef = chunk.getReferenceTo(index);
            buffer.tryRemoveComponent(selfRef, MountedComponent.getComponentType());
            CleanupUtils.safeRemoveMountParent(buffer, selfRef);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] MountOrphanReaperSystem failed: %s", e.getMessage());
        }
    }
}
