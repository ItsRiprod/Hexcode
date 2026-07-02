package com.riprod.hexcode.builtin.hexCore.execution.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class ExecutionConfig extends HexConfigAsset {

    public Hex getHex(ComponentAccessor<EntityStore> accessor, HexRoot hexRoot) {
        var playerRef = hexRoot.getSourceRef(accessor);
        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }
        ExecutionComponent execution = accessor.getComponent(playerRef, ExecutionComponent.getComponentType());
        return execution != null ? execution.getQueuedHex() : null;
    }

    public static final BuilderCodec<ExecutionConfig> CODEC = BuilderCodec
            .builder(ExecutionConfig.class, ExecutionConfig::new, ExecutionConfig.BASE_CODEC)
            .build();
}
