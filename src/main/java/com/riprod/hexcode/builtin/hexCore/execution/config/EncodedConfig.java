package com.riprod.hexcode.builtin.hexCore.execution.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.root.HexRoot;
import com.riprod.hexcode.core.common.hexes.codec.HexFieldCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;

public class EncodedConfig extends HexConfigAsset {

    private Hex hex;

    public Hex getHex(ComponentAccessor<EntityStore> accessor, HexRoot hexRoot) {
        return hex;
    }

    public static final BuilderCodec<EncodedConfig> CODEC = BuilderCodec
            .builder(EncodedConfig.class, EncodedConfig::new, EncodedConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Hex", HexFieldCodec.PLAYER),
                    (h, v) -> h.hex = v,
                    h -> h.hex)
            .add()
            .build();
}
