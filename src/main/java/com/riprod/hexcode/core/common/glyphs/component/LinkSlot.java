package com.riprod.hexcode.core.common.glyphs.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;

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
