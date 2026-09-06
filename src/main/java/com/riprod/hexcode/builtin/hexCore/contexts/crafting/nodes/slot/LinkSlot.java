package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.component.Slot;

public final class LinkSlot extends Slot {
    public static final BuilderCodec<LinkSlot> CODEC = BuilderCodec
            .builder(LinkSlot.class, LinkSlot::new, Slot.BASE_CODEC)
            .build();

    @Override
    public Slot clone() {
        LinkSlot copy = new LinkSlot();
        copyBaseState(copy);
        return copy;
    }
}
