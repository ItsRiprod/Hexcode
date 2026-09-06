package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.named;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.registry.SlotConfig;
import com.riprod.hexcode.core.common.node.NodeInterface;

public final class NamedSlotConfig extends SlotConfig {
    public static final String TYPE = "Named";

    public static final BuilderCodec<NamedSlotConfig> CODEC = BuilderCodec
            .builder(NamedSlotConfig.class, NamedSlotConfig::new, SlotConfig.BASE_CODEC)
            .build();

    @Override
    public Slot create() {
        return new NamedSlot();
    }

    @Override
    public NodeInterface handler() {
        return NamedSlotHandler.INSTANCE;
    }

    @Override
    public boolean defaultUnique() {
        return true;
    }
}
