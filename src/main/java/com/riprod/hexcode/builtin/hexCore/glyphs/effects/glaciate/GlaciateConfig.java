package com.riprod.hexcode.builtin.hexCore.glyphs.effects.glaciate;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class GlaciateConfig extends GlyphConfig {

    public static final GlaciateConfig DEFAULTS = new GlaciateConfig();

    private float iceScale = 2.0f;
    private double minDamageSpeed = 0.1;
    private double knockbackScale = 0.3;
    private double minVerticalKnockback = 2.0;
    private float damageRadius = 1.2f;
    private float damageMultiplier = 1.0f;
    private String iceModelId = "Glaciate_Ice";
    private String hardCollisionConfigId = "Hexcode_Glaciate_HardCollision";

    public float getIceScale() {
        return iceScale;
    }

    public double getMinDamageSpeed() {
        return minDamageSpeed;
    }

    public double getKnockbackScale() {
        return knockbackScale;
    }

    public double getMinVerticalKnockback() {
        return minVerticalKnockback;
    }

    public float getDamageRadius() {
        return damageRadius;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public String getIceModelId() {
        return iceModelId;
    }

    public String getHardCollisionConfigId() {
        return hardCollisionConfigId;
    }

    public static final BuilderCodec<GlaciateConfig> CODEC = BuilderCodec
            .builder(GlaciateConfig.class, GlaciateConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("IceScale", Codec.FLOAT, true),
                    (c, v) -> c.iceScale = v, c -> c.iceScale)
            .add()
            .append(new KeyedCodec<>("MinDamageSpeed", Codec.DOUBLE, true),
                    (c, v) -> c.minDamageSpeed = v, c -> c.minDamageSpeed)
            .add()
            .append(new KeyedCodec<>("KnockbackScale", Codec.DOUBLE, true),
                    (c, v) -> c.knockbackScale = v, c -> c.knockbackScale)
            .add()
            .append(new KeyedCodec<>("MinVerticalKnockback", Codec.DOUBLE, true),
                    (c, v) -> c.minVerticalKnockback = v, c -> c.minVerticalKnockback)
            .add()
            .append(new KeyedCodec<>("DamageRadius", Codec.FLOAT, true),
                    (c, v) -> c.damageRadius = v, c -> c.damageRadius)
            .add()
            .append(new KeyedCodec<>("DamageMultiplier", Codec.FLOAT, true),
                    (c, v) -> c.damageMultiplier = v, c -> c.damageMultiplier)
            .add()
            .append(new KeyedCodec<>("IceModel", Codec.STRING, true),
                    (c, v) -> c.iceModelId = v, c -> c.iceModelId)
            .addValidatorLate(() -> ModelAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("HardCollisionConfig", Codec.STRING, true),
                    (c, v) -> c.hardCollisionConfigId = v, c -> c.hardCollisionConfigId)
            .addValidatorLate(() -> HitboxCollisionConfig.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
