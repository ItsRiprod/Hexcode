package com.riprod.hexcode.builtin.hexCore.glyphs.effects.ensnare;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class EnsnareConfig extends GlyphConfig {

    public static final EnsnareConfig DEFAULTS = new EnsnareConfig();

    private float spikeScale = 0.5f;
    private float damageCooldownSeconds = 1.0f;
    private int maxSpikes = 64;
    private int groundScanRange = 3;
    private float density = 0.5f;
    private int heightTolerance = 2;
    private float spikeDamage = 0.0f;
    private double spikeHitRadiusSq = 0.49;
    private double boxPaddingXZ = 1.0;
    private double boxPaddingYMin = 3.0;
    private double boxPaddingYMax = 4.0;
    private double spikeHitYMin = -0.5;
    private double spikeHitYMax = 1.5;

    public float getSpikeScale() {
        return spikeScale;
    }

    public float getDamageCooldownSeconds() {
        return damageCooldownSeconds;
    }

    public int getMaxSpikes() {
        return maxSpikes;
    }

    public int getGroundScanRange() {
        return groundScanRange;
    }

    public float getDensity() {
        return density;
    }

    public int getHeightTolerance() {
        return heightTolerance;
    }

    public float getSpikeDamage() {
        return spikeDamage;
    }

    public double getSpikeHitRadiusSq() {
        return spikeHitRadiusSq;
    }

    public double getBoxPaddingXZ() {
        return boxPaddingXZ;
    }

    public double getBoxPaddingYMin() {
        return boxPaddingYMin;
    }

    public double getBoxPaddingYMax() {
        return boxPaddingYMax;
    }

    public double getSpikeHitYMin() {
        return spikeHitYMin;
    }

    public double getSpikeHitYMax() {
        return spikeHitYMax;
    }

    public static final BuilderCodec<EnsnareConfig> CODEC = BuilderCodec
            .builder(EnsnareConfig.class, EnsnareConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("SpikeScale", Codec.FLOAT, true),
                    (c, v) -> c.spikeScale = v, c -> c.spikeScale)
            .add()
            .append(new KeyedCodec<>("DamageCooldownSeconds", Codec.FLOAT, true),
                    (c, v) -> c.damageCooldownSeconds = v, c -> c.damageCooldownSeconds)
            .add()
            .append(new KeyedCodec<>("MaxSpikes", Codec.INTEGER, true),
                    (c, v) -> c.maxSpikes = v, c -> c.maxSpikes)
            .add()
            .append(new KeyedCodec<>("GroundScanRange", Codec.INTEGER, true),
                    (c, v) -> c.groundScanRange = v, c -> c.groundScanRange)
            .add()
            .append(new KeyedCodec<>("Density", Codec.FLOAT, true),
                    (c, v) -> c.density = v, c -> c.density)
            .add()
            .append(new KeyedCodec<>("HeightTolerance", Codec.INTEGER, true),
                    (c, v) -> c.heightTolerance = v, c -> c.heightTolerance)
            .add()
            .append(new KeyedCodec<>("SpikeDamage", Codec.FLOAT, true),
                    (c, v) -> c.spikeDamage = v, c -> c.spikeDamage)
            .add()
            .append(new KeyedCodec<>("SpikeHitRadiusSq", Codec.DOUBLE, true),
                    (c, v) -> c.spikeHitRadiusSq = v, c -> c.spikeHitRadiusSq)
            .add()
            .append(new KeyedCodec<>("BoxPaddingXZ", Codec.DOUBLE, true),
                    (c, v) -> c.boxPaddingXZ = v, c -> c.boxPaddingXZ)
            .add()
            .append(new KeyedCodec<>("BoxPaddingYMin", Codec.DOUBLE, true),
                    (c, v) -> c.boxPaddingYMin = v, c -> c.boxPaddingYMin)
            .add()
            .append(new KeyedCodec<>("BoxPaddingYMax", Codec.DOUBLE, true),
                    (c, v) -> c.boxPaddingYMax = v, c -> c.boxPaddingYMax)
            .add()
            .append(new KeyedCodec<>("SpikeHitYMin", Codec.DOUBLE, true),
                    (c, v) -> c.spikeHitYMin = v, c -> c.spikeHitYMin)
            .add()
            .append(new KeyedCodec<>("SpikeHitYMax", Codec.DOUBLE, true),
                    (c, v) -> c.spikeHitYMax = v, c -> c.spikeHitYMax)
            .add()
            .build();
}
