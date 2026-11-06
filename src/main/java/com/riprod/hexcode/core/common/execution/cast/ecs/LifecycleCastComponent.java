package com.riprod.hexcode.core.common.execution.cast.ecs;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.BranchEndEvent;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public interface LifecycleCastComponent extends CastComponent {

    default void onBranchOpen(@Nonnull HexContext ctx, @Nonnull CommandBuffer<EntityStore> buffer) {
    }

    default void onBranchEnd(@Nonnull HexContext ctx, @Nonnull BranchEndEvent.Reason reason,
            @Nonnull CommandBuffer<EntityStore> buffer) {
    }

    default void onCastEnd(@Nonnull HexCast cast, @Nonnull CommandBuffer<EntityStore> buffer) {
    }
}
