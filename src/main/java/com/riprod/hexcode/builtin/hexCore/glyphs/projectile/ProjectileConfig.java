package com.riprod.hexcode.builtin.hexCore.glyphs.projectile;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class ProjectileConfig extends GlyphConfig {

    public static final ProjectileConfig DEFAULTS = new ProjectileConfig();

    private float projectileScale = 0.5f;
    private double ttlSeconds = 600.0;
    private double spawnOffset = 1.1;
    private String hitRootInteraction = "Hex_Projectile_Hit";
    private String missRootInteraction = "Hex_Projectile_Miss";
    private String bounceRootInteraction = "Hex_Projectile_Bounce";

    public float getProjectileScale() {
        return projectileScale;
    }

    public double getTtlSeconds() {
        return ttlSeconds;
    }

    public double getSpawnOffset() {
        return spawnOffset;
    }

    public String getHitRootInteraction() {
        return hitRootInteraction;
    }

    public String getMissRootInteraction() {
        return missRootInteraction;
    }

    public String getBounceRootInteraction() {
        return bounceRootInteraction;
    }

    public static final BuilderCodec<ProjectileConfig> CODEC = BuilderCodec
            .builder(ProjectileConfig.class, ProjectileConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("ProjectileScale", Codec.FLOAT, true),
                    (c, v) -> c.projectileScale = v, c -> c.projectileScale)
            .add()
            .append(new KeyedCodec<>("TtlSeconds", Codec.DOUBLE, true),
                    (c, v) -> c.ttlSeconds = v, c -> c.ttlSeconds)
            .add()
            .append(new KeyedCodec<>("SpawnOffset", Codec.DOUBLE, true),
                    (c, v) -> c.spawnOffset = v, c -> c.spawnOffset)
            .add()
            .append(new KeyedCodec<>("HitRootInteraction", RootInteraction.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.hitRootInteraction = v, c -> c.hitRootInteraction)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("MissRootInteraction", RootInteraction.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.missRootInteraction = v, c -> c.missRootInteraction)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("BounceRootInteraction", RootInteraction.CHILD_ASSET_CODEC, true),
                    (c, v) -> c.bounceRootInteraction = v, c -> c.bounceRootInteraction)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
