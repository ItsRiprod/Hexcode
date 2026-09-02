package com.riprod.hexcode.core.common.obelisk;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface ObeliskSessionState {

    default void onTeardown(CommandBuffer<EntityStore> buffer) {
    }
}
