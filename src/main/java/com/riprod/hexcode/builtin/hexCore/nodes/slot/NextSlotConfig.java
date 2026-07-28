package com.riprod.hexcode.builtin.hexCore.nodes.slot;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class NextSlotConfig extends SlotConfig {
    public static final String TYPE = "Next";

    public static final BuilderCodec<NextSlotConfig> CODEC = BuilderCodec
            .builder(NextSlotConfig.class, NextSlotConfig::new, SlotConfig.BASE_CODEC)
            .build();

    @Override
    public Slot create() {
        return new LinkSlot();
    }

    @Override
    public NodeInterface handler() {
        return SlotNodeHandler.INSTANCE;
    }

    @Override
    public boolean defaultUnique() {
        return false;
    }

    @Override
    public boolean isFlow() {
        return true;
    }
}
