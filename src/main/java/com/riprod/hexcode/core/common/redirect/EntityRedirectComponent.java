package com.riprod.hexcode.core.common.redirect;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EntityRedirectComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, EntityRedirectComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, EntityRedirectComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, EntityRedirectComponent> getComponentType() {
        return componentType;
    }

    private PersistentRef owner;
    private PersistentRef deferral;

    public EntityRedirectComponent() {
    }

    public EntityRedirectComponent(PersistentRef owner, PersistentRef deferral) {
        this.owner = owner;
        this.deferral = deferral;
    }

    @Nullable
    public UUID getDeferralUuid() {
        return deferral != null ? deferral.getUuid() : null;
    }

    @Nullable
    public Ref<EntityStore> resolveDeferral(ComponentAccessor<EntityStore> accessor) {
        if (owner == null || deferral == null) return null;
        Ref<EntityStore> ownerRef = owner.getEntity(accessor);
        if (ownerRef == null || !ownerRef.isValid()) return null;
        Ref<EntityStore> deferralRef = deferral.getEntity(accessor);
        if (deferralRef == null || !deferralRef.isValid()) return null;
        return deferralRef;
    }

    @Nonnull
    @Override
    public EntityRedirectComponent clone() {
        return new EntityRedirectComponent(owner, deferral);
    }
}
