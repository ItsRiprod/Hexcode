package com.riprod.hexcode.core.common.triggers.component;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record TriggerEvent(@Nonnull String key,
                           @Nullable UUID subjectUuid,
                           @Nullable Ref<EntityStore> subjectRef,
                           @Nullable Object payload) {

    public TriggerEvent(@Nonnull String key, @Nullable UUID subjectUuid, @Nullable Ref<EntityStore> subjectRef) {
        this(key, subjectUuid, subjectRef, null);
    }
}
