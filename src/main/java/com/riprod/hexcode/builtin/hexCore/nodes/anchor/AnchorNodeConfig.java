package com.riprod.hexcode.builtin.hexCore.nodes.anchor;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class AnchorNodeConfig extends NodeConfig {
    public static final String TYPE = "Anchor";

    public static final BuilderCodec<AnchorNodeConfig> CODEC = BuilderCodec
            .builder(AnchorNodeConfig.class, AnchorNodeConfig::new, NodeConfig.BASE_CODEC)
            .build();

    @Override
    public NodeInterface handler() {
        return AnchorNodeHandler.INSTANCE;
    }
}
