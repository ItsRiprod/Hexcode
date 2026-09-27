package com.riprod.hexcode.plugins.hexCore.execution.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;

public class OverlayConfig extends HexConfigAsset {

    public static final BuilderCodec<OverlayConfig> CODEC = BuilderCodec
            .builder(OverlayConfig.class, OverlayConfig::new, OverlayConfig.BASE_CODEC)
            .build();
}
