package com.riprod.hexcode.builtin.hexCore.execution.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.component.HexRoot;
import com.riprod.hexcode.core.common.execution.component.HexcasterIdleComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class StaffConfig extends HexConfigAsset {
    public Hex getHex(ComponentAccessor<EntityStore> accessor, HexRoot hexRoot) {
        var playerRef = hexRoot.getSourceRef(accessor);
        HexcasterIdleComponent idleComp = accessor.getComponent(playerRef, HexcasterIdleComponent.getComponentType());
        return idleComp.getActiveHex();
    }

    public static final BuilderCodec<StaffConfig> CODEC = BuilderCodec
            .builder(StaffConfig.class, StaffConfig::new, StaffConfig.BASE_CODEC)
            .build();
}
