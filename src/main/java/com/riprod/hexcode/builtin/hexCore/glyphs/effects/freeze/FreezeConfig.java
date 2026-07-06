package com.riprod.hexcode.builtin.hexCore.glyphs.effects.freeze;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class FreezeConfig extends GlyphConfig {

    public static final FreezeConfig DEFAULTS = new FreezeConfig();

    private String iceBlockId = "Rock_Ice";

    public String getIceBlockId() {
        return iceBlockId;
    }

    public static final BuilderCodec<FreezeConfig> CODEC = BuilderCodec
            .builder(FreezeConfig.class, FreezeConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("IceBlock", Codec.STRING, true),
                    (c, v) -> c.iceBlockId = v, c -> c.iceBlockId)
            .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
