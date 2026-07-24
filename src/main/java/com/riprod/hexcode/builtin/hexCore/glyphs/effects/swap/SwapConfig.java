package com.riprod.hexcode.builtin.hexCore.glyphs.effects.swap;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class SwapConfig extends GlyphConfig {

    public static final SwapConfig DEFAULTS = new SwapConfig();

    private String effectId = "Teleportation";

    public String getEffectId() {
        return effectId;
    }

    public static final BuilderCodec<SwapConfig> CODEC = BuilderCodec
            .builder(SwapConfig.class, SwapConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("Effect", EntityEffect.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.effectId = v, c -> c.effectId)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
