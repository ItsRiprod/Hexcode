package com.riprod.hexcode.builtin.hexCore.contexts.selecting.nodes.preview;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class PreviewNodeConfig extends NodeConfig {
    public static final String TYPE = "Container";

    public static final BuilderCodec<PreviewNodeConfig> CODEC = BuilderCodec
            .builder(PreviewNodeConfig.class, PreviewNodeConfig::new, NodeConfig.BASE_CODEC)
            .build();

    @Override
    public NodeInterface handler() {
        return PreviewNodeHandler.INSTANCE;
    }
}
