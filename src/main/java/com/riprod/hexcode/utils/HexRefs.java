package com.riprod.hexcode.utils;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class HexRefs {

    private HexRefs() {
    }

    @Nullable
    public static Ref<EntityStore> live(@Nullable Ref<EntityStore> ref,
            @Nullable ComponentAccessor<EntityStore> accessor) {
        if (ref == null || accessor == null || !ref.isValid()) {
            return null;
        }
        return ref.getStore() == accessor.getExternalData().getStore() ? ref : null;
    }

    public static boolean isLive(@Nullable Ref<EntityStore> ref,
            @Nullable ComponentAccessor<EntityStore> accessor) {
        return live(ref, accessor) != null;
    }

    @Nullable
    public static Ref<EntityStore> resolve(@Nullable PersistentRef persistent,
            @Nullable ComponentAccessor<EntityStore> accessor) {
        if (persistent == null || accessor == null || !persistent.isValid()) {
            return null;
        }

        Ref<EntityStore> ref = persistent.getEntity(accessor);
        if (ref == null || ref.getStore() == accessor.getExternalData().getStore()) {
            return ref;
        }

        persistent.setUuid(persistent.getUuid());
        return persistent.getEntity(accessor);
    }
}
