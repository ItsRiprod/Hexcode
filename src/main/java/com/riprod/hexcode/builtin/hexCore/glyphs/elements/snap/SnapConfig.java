package com.riprod.hexcode.builtin.hexCore.glyphs.elements.snap;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphConfig;

public final class SnapConfig extends ElementGlyphConfig {

    public static final SnapConfig DEFAULTS = new SnapConfig();

    private String damageCause = "Ice";
    private String onHitEffect = "Ice";
    private float onHitDuration = 1.5f;

    public String getDamageCause() {
        return damageCause;
    }

    public String getOnHitEffect() {
        return onHitEffect;
    }

    public float getOnHitDuration() {
        return onHitDuration;
    }

    public static final BuilderCodec<SnapConfig> CODEC = BuilderCodec
            .builder(SnapConfig.class, SnapConfig::new, ElementGlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING, true),
                    (c, v) -> c.damageCause = v, c -> c.damageCause)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("OnHitEffect", Codec.STRING, true),
                    (c, v) -> c.onHitEffect = v, c -> c.onHitEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("OnHitDuration", Codec.FLOAT, true),
                    (c, v) -> c.onHitDuration = v, c -> c.onHitDuration)
            .add()
            .build();
}
