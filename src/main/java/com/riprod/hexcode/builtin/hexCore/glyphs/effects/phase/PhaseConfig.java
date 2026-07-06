package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class PhaseConfig extends GlyphConfig {

    public static final PhaseConfig DEFAULTS = new PhaseConfig();

    private double crushDamage = 4.0;
    private String damageCauseId = "Environment";

    public double getCrushDamage() {
        return crushDamage;
    }

    public String getDamageCauseId() {
        return damageCauseId;
    }

    public static final BuilderCodec<PhaseConfig> CODEC = BuilderCodec
            .builder(PhaseConfig.class, PhaseConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("CrushDamage", Codec.DOUBLE, true),
                    (c, v) -> c.crushDamage = v, c -> c.crushDamage)
            .add()
            .append(new KeyedCodec<>("DamageCause", DamageCause.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.damageCauseId = v, c -> c.damageCauseId)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
