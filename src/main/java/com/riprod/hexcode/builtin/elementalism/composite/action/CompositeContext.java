package com.riprod.hexcode.builtin.elementalism.composite.action;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.elementalism.composite.CompositeAsset;
import com.riprod.hexcode.builtin.elementalism.tier.ElementalTier;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public record CompositeContext(
        CompositeAsset composite,
        String existingStatusId,
        ElementalTier incoming,
        HexContext incomingContext,
        CommandBuffer<EntityStore> buffer,
        @Nullable Ref<EntityStore> entity,
        @Nullable Vector3i block) {
}
