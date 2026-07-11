package com.riprod.hexcode.builtin.hexCore.glyphs.effects.gust;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class GustConfig extends GlyphConfig {

    public static final GustConfig DEFAULTS = new GustConfig();

    // purposefully a random number to avoid intentional offsets causing the divide-by-zero bug (obtained by keyboard spam)
    private double minKnockbackOffset = 0.15792347;
    private float entityDamage = 0.0f;
    private float entityDamageFalloff = 1.0f;
    private double verticalKnockbackScale = 0.3;

    public double getMinKnockbackOffset() {
        return minKnockbackOffset;
    }

    public float getEntityDamage() {
        return entityDamage;
    }

    public float getEntityDamageFalloff() {
        return entityDamageFalloff;
    }

    public double getVerticalKnockbackScale() {
        return verticalKnockbackScale;
    }

    public static final BuilderCodec<GustConfig> CODEC = BuilderCodec
            .builder(GustConfig.class, GustConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("MinKnockbackOffset", Codec.DOUBLE, true),
                    (c, v) -> c.minKnockbackOffset = v, c -> c.minKnockbackOffset)
            .add()
            .append(new KeyedCodec<>("EntityDamage", Codec.FLOAT, true),
                    (c, v) -> c.entityDamage = v, c -> c.entityDamage)
            .add()
            .append(new KeyedCodec<>("EntityDamageFalloff", Codec.FLOAT, true),
                    (c, v) -> c.entityDamageFalloff = v, c -> c.entityDamageFalloff)
            .add()
            .append(new KeyedCodec<>("VerticalKnockbackScale", Codec.DOUBLE, true),
                    (c, v) -> c.verticalKnockbackScale = v, c -> c.verticalKnockbackScale)
            .add()
            .build();
}
