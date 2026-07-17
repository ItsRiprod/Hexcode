package com.riprod.hexcode.builtin.hexCore.nodes.container;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class ContainerNodeConfig extends NodeConfig {
    public static final String TYPE = "Container";

    public static final BuilderCodec<ContainerNodeConfig> CODEC = BuilderCodec
            .builder(ContainerNodeConfig.class, ContainerNodeConfig::new, NodeConfig.BASE_CODEC)
            .build();

    @Override
    public NodeInterface handler() {
        return ContainerNodeHandler.INSTANCE;
    }
}
