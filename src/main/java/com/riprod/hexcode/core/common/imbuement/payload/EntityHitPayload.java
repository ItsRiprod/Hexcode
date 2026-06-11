package com.riprod.hexcode.core.common.imbuement.payload;

import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.triggers.registry.DefaultVariableKind;


public record EntityHitPayload(@Nullable Ref<EntityStore> target, @Nullable UUID targetUuid)
        implements DefaultVariableKind.TargetedPayload {
}
