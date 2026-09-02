package com.riprod.hexcode.api.execution;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public interface CastTransform {

    void apply(HexContext context, ComponentAccessor<EntityStore> accessor);
}
