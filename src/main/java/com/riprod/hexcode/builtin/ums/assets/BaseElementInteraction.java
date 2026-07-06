package com.riprod.hexcode.builtin.ums.assets;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.riprod.hexcode.builtin.ums.registry.UmsReactionContext;

public abstract class BaseElementInteraction {

    public static final CodecMapCodec<BaseElementInteraction> CODEC = new CodecMapCodec<>("Type");

    public static final BuilderCodec<BaseElementInteraction> BASE_CODEC = BuilderCodec
            .abstractBuilder(BaseElementInteraction.class)
            .build();

    public abstract void apply(UmsReactionContext ctx);
}
