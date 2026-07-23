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
            @Nonnull Ref<EntityStore> ownerRef, @Nonnull Ref<EntityStore> deferralRef) {
        PersistentRef owner = new PersistentRef();
        owner.setEntity(ownerRef, buffer);
        PersistentRef deferral = new PersistentRef();
        deferral.setEntity(deferralRef, buffer);
        buffer.putComponent(targetRef, EntityRedirectComponent.getComponentType(),
                new EntityRedirectComponent(owner, deferral));
    }

    // removes the marker only when the ward still points at deferralUuid; a mismatch means
    // a later concentration overwrote it and now owns the teardown.
    public static void unstamp(@Nonnull CommandBuffer<EntityStore> buffer, @Nullable Ref<EntityStore> targetRef,
            @Nullable UUID deferralUuid) {
        if (targetRef == null || deferralUuid == null || !targetRef.isValid()) return;
        EntityRedirectComponent comp = buffer.getComponent(targetRef, EntityRedirectComponent.getComponentType());
        if (comp == null) return;
        if (deferralUuid.equals(comp.getDeferralUuid())) {
            buffer.tryRemoveComponent(targetRef, EntityRedirectComponent.getComponentType());
        }
    }
}
