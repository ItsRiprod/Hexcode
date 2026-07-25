package com.riprod.hexcode.builtin.hexCore.glyphs.effects.phase;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class PhaseConfig extends GlyphConfig {

    public static final PhaseConfig DEFAULTS = new PhaseConfig();

    @Nullable
    private Impact crushDamageImpact;
    private String damageCauseId = "Environment";

    @Nullable
    public Impact getCrushDamageImpact() {
        return crushDamageImpact;
    }

    public String getDamageCauseId() {
        return damageCauseId;
    }

    public static final BuilderCodec<PhaseConfig> CODEC = BuilderCodec
            .builder(PhaseConfig.class, PhaseConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("CrushDamageImpact", Impact.CODEC),
                    (c, v) -> c.crushDamageImpact = v, c -> c.crushDamageImpact)
            .add()
            .append(new KeyedCodec<>("DamageCause", DamageCause.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.damageCauseId = v, c -> c.damageCauseId)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
