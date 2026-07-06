package com.riprod.hexcode.builtin.ums.registry;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record UmsReactionContext(
        CommandBuffer<EntityStore> accessor,
        Ref<EntityStore> targetRef,
        Damage damage,
        String attackerCauseId,
        String elementId,
        @Nullable Vector3d hitPosition) {
}
