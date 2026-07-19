package com.riprod.hexcode.builtin.hexCore.glyphs.effects.disguise;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class DisguiseConfig extends GlyphConfig {

    public static final DisguiseConfig DEFAULTS = new DisguiseConfig();

    private boolean disguiseNametag = true;

    public boolean disguiseNametag() {
        return disguiseNametag;
    }

    public static final BuilderCodec<DisguiseConfig> CODEC = BuilderCodec
            .builder(DisguiseConfig.class, DisguiseConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DisguiseNametag", Codec.BOOLEAN, true),
                    (c, v) -> c.disguiseNametag = v, c -> c.disguiseNametag)
            .add()
            .build();
}
