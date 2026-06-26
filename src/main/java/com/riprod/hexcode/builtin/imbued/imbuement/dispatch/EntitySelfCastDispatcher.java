package com.riprod.hexcode.builtin.imbued.imbuement.dispatch;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.imbuement.dispatch.CastRootDispatcher;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;

public final class EntitySelfCastDispatcher implements CastRootDispatcher {
    @Override
    public void dispatch(@Nonnull Trigger trigger, @Nonnull TriggerEvent event,
            @Nonnull CommandBuffer<EntityStore> buffer) {
    }
}
