package com.riprod.hexcode.builtin.hexCore.execution.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class ExecutionConfig extends HexConfigAsset {

    @Override
    protected Hex resolveHex(Ref<EntityStore> root, ComponentAccessor<EntityStore> accessor) {
        var execution = accessor.getComponent(root, ExecutionComponent.getComponentType());
        return execution != null ? execution.getQueuedHex() : null;
    }

    public static final BuilderCodec<ExecutionConfig> CODEC = BuilderCodec
            .builder(ExecutionConfig.class, ExecutionConfig::new, ExecutionConfig.BASE_CODEC)
            .build();
}
