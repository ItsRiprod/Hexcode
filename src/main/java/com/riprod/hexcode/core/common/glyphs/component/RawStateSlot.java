package com.riprod.hexcode.core.common.glyphs.component;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class RawStateSlot extends Slot {

    public static final String TYPE = "Raw";

    private byte[] state;

    public static final BuilderCodec<RawStateSlot> CODEC = BuilderCodec
            .builder(RawStateSlot.class, RawStateSlot::new, Slot.BASE_CODEC)
            .append(new KeyedCodec<>("State", Codec.BYTE_ARRAY),
                    (s, v) -> s.state = v,
                    s -> s.state)
            .add()
            .build();

    @Override
    @Nullable
    public byte[] encodeState() {
        return state;
    }

    @Override
    public void decodeState(byte[] state) {
        this.state = state;
    }

    @Override
    public Slot clone() {
        RawStateSlot copy = new RawStateSlot();
        copyBaseState(copy);
        copy.state = state != null ? Arrays.copyOf(state, state.length) : null;
        return copy;
    }
}
