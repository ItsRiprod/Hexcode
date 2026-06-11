package com.riprod.hexcode.core.common.imbuement.payload;

import java.util.UUID;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.triggers.registry.DefaultVariableKind;

public record EntityDamagedPayload(@Nullable Ref<EntityStore> attacker, @Nullable UUID attackerUuid)
        implements DefaultVariableKind.AttackedPayload {
}
