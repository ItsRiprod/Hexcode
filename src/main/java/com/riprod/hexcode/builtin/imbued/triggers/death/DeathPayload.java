package com.riprod.hexcode.builtin.imbued.triggers.death;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record DeathPayload(Ref<EntityStore> deceased,
                            @Nullable Ref<EntityStore> killer) {
}
