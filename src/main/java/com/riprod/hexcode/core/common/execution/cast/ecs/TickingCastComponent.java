package com.riprod.hexcode.core.common.execution.cast.ecs;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public interface TickingCastComponent extends CastComponent {

    void onTick(float dt, @Nonnull Ref<EntityStore> host, @Nonnull HexContext ctx,
            @Nonnull CommandBuffer<EntityStore> buffer);
}
