package com.riprod.hexcode.builtin.hexCore.glyphs.elements.drown;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class DrownConfig extends ElementGlyphConfig {

    public static final DrownConfig DEFAULTS = new DrownConfig();

    private String damageCause = "Water";

    public String getDamageCause() {
        return damageCause;
    }

    public static final BuilderCodec<DrownConfig> CODEC = BuilderCodec
            .builder(DrownConfig.class, DrownConfig::new, ElementGlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING, true),
                    (c, v) -> c.damageCause = v, c -> c.damageCause)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
