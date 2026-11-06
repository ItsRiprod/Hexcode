package com.riprod.hexcode.builtin.elementalism.element.efficiency;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.riprod.hexcode.builtin.elementalism.element.ElementTickInput;

public abstract class BranchEfficiency {
    public static final CodecMapCodec<BranchEfficiency> CODEC = new CodecMapCodec<>("Type");

    public abstract float efficiency(ElementTickInput input);
}
