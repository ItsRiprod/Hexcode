package com.riprod.hexcode.builtin.hexCore.glyphs.elements.bolt;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class BoltConfig extends ElementGlyphConfig {

    public static final BoltConfig DEFAULTS = new BoltConfig();

    private String damageCause = "Lightning";

    public String getDamageCause() {
        return damageCause;
    }

    public static final BuilderCodec<BoltConfig> CODEC = BuilderCodec
            .builder(BoltConfig.class, BoltConfig::new, ElementGlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING, true),
                    (c, v) -> c.damageCause = v, c -> c.damageCause)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
