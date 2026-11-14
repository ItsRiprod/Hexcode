package com.riprod.hexcode.core.common.triggers.component;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.riprod.hexcode.core.common.triggers.handler.TriggerCallback;

public record TriggerSubscription(@Nonnull UUID id,
                                   @Nonnull String key,
                                   @Nullable UUID subjectUuid,
                                   @Nonnull TriggerCallback callback,
                                   boolean oneShot) {

    public static TriggerSubscription bootstrap(@Nonnull String key,
                                                @Nonnull TriggerCallback callback) {
        return new TriggerSubscription(UUID.randomUUID(), key, null, callback, false);
    }
}
