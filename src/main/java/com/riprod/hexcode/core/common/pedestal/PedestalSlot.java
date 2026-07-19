package com.riprod.hexcode.core.common.pedestal;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class PedestalSlot {

    public static final BuilderCodec<PedestalSlot> CODEC = BuilderCodec
            .builder(PedestalSlot.class, PedestalSlot::new)
            .append(new KeyedCodec<>("Label", Codec.STRING),
                    (s, v) -> s.label = v, s -> s.label)
            .add()
            .append(new KeyedCodec<>("Description", Codec.STRING),
                    (s, v) -> s.description = v, s -> s.description)
            .add()
            .build();

    private String label;
    private String description;

    public PedestalSlot() {
    }

    public static PedestalSlot of(String label, String description) {
        PedestalSlot slot = new PedestalSlot();
        slot.label = label;
        slot.description = description;
        return slot;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }
}
