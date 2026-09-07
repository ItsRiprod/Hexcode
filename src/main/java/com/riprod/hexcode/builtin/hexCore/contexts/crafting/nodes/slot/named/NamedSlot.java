package com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.named;

import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import com.riprod.hexcode.core.common.glyphs.component.Slot;

public final class NamedSlot extends Slot {

    private static final int MAX_STATE_BYTES = 255;

    @Nullable
    private String value;

    public static final BuilderCodec<NamedSlot> CODEC = BuilderCodec
            .builder(NamedSlot.class, NamedSlot::new, Slot.BASE_CODEC)
            .append(new KeyedCodec<>("Value", Codec.STRING),
                    (s, v) -> s.setValue(v),
                    s -> s.value)
            .add()
            .build();

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value != null && value.isBlank() ? null : value;
    }

    @Override
    @Nullable
    public byte[] encodeState() {
        if (value == null) return null;
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_STATE_BYTES) return bytes;
        int end = MAX_STATE_BYTES;
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) end--;
        byte[] clamped = new byte[end];
        System.arraycopy(bytes, 0, clamped, 0, end);
        return clamped;
    }

    @Override
    public void decodeState(byte[] state) {
        setValue(new String(state, StandardCharsets.UTF_8));
    }

    @Override
    public Message displayMessage() {
        return value != null ? Message.raw(value) : super.displayMessage();
    }

    @Override
    public Slot clone() {
        NamedSlot copy = new NamedSlot();
        copyBaseState(copy);
        copy.value = this.value;
        return copy;
    }
}
