package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.glyph;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.node.NodeConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class GlyphNodeConfig extends NodeConfig {
    public static final String TYPE = "Glyph";

    public static final BuilderCodec<GlyphNodeConfig> CODEC = BuilderCodec
            .builder(GlyphNodeConfig.class, GlyphNodeConfig::new, NodeConfig.BASE_CODEC)
            .build();

    @Override
    public NodeInterface handler() {
        return GlyphNodeHandler.INSTANCE;
    }
}
