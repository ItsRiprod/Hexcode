package com.riprod.hexcode.core.common.redirect;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class EntityRedirectSpawner {

    private EntityRedirectSpawner() {
    }

    public static void stamp(@Nonnull CommandBuffer<EntityStore> buffer, @Nonnull Ref<EntityStore> targetRef,
            @Nonnull Ref<EntityStore> ownerRef, @Nonnull Ref<EntityStore> deferralRef, @Nonnull UUID stampId) {
        PersistentRef owner = new PersistentRef();
        owner.setEntity(ownerRef, buffer);
        PersistentRef deferral = new PersistentRef();
        deferral.setEntity(deferralRef, buffer);
        buffer.putComponent(targetRef, EntityRedirectComponent.getComponentType(),
                new EntityRedirectComponent(owner, deferral, stampId));
    }

    // removes the marker only when it still carries stampId; a mismatch means a later stamp
    // overwrote it and now owns the teardown
    public static void unstamp(@Nonnull CommandBuffer<EntityStore> buffer, @Nullable Ref<EntityStore> targetRef,
            @Nullable UUID stampId) {
        if (targetRef == null || stampId == null || !targetRef.isValid()) return;
        EntityRedirectComponent comp = buffer.getComponent(targetRef, EntityRedirectComponent.getComponentType());
        if (comp == null) return;
        if (stampId.equals(comp.getStampId())) {
            buffer.tryRemoveComponent(targetRef, EntityRedirectComponent.getComponentType());
        }
    }
}
