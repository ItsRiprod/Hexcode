package com.riprod.hexcode.builtin.elementalism.composite.action;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;

public final class CompositeActions {
    public static final CodecMapCodec<CompositeAction> CODEC = new CodecMapCodec<>("Type");

    private CompositeActions() {
    }
}
