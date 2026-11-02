package com.riprod.hexcode.core.common.execution.cast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public interface CastAssembly {

    @Nonnull
    Ref<EntityStore> root();

    @Nonnull
    CommandBuffer<EntityStore> accessor();

    @Nullable
    <T extends CastComponent> T get(@Nonnull CastComponentType<T> type);
}
