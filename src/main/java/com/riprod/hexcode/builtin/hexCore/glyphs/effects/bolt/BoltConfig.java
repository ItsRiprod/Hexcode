package com.riprod.hexcode.builtin.hexCore.glyphs.effects.bolt;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class BoltConfig extends GlyphConfig {

    public static final BoltConfig DEFAULTS = new BoltConfig();

    private String damageCauseId = "Environment";

    public String getDamageCauseId() {
        return damageCauseId;
    }

    public static final BuilderCodec<BoltConfig> CODEC = BuilderCodec
            .builder(BoltConfig.class, BoltConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DamageCause", DamageCause.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.damageCauseId = v, c -> c.damageCauseId)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
