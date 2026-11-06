package com.riprod.hexcode.core.common.effects.component;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;

public class GlyphEffect {
    public static final BuilderCodec<GlyphEffect> CODEC;

    public enum Tint {
        Primary, Secondary, None
    }

    private ModelParticle[] particles;
    private String sound;
    private Tint tint = Tint.Primary;
    private ModelParticle[] pulse;
    private float interval = 1.0f;
    private ModelTrail trail;
    private float light;

    @Nullable
    public ModelParticle[] getParticles() {
        return this.particles;
    }

    @Nullable
    public String getSound() {
        return this.sound;
    }

    public Tint getTint() {
        return this.tint;
    }

    @Nullable
    public ModelParticle[] getPulse() {
        return this.pulse;
    }

    public float getInterval() {
        return this.interval;
    }

    @Nullable
    public ModelTrail getTrail() {
        return this.trail;
    }

    public float getLight() {
        return this.light;
    }

    static {
        CODEC = BuilderCodec.builder(GlyphEffect.class, GlyphEffect::new)
                .append(new KeyedCodec<>("Particles", ModelParticle.ARRAY_CODEC),
                        (e, v) -> e.particles = v, e -> e.particles)
                .add()
                .append(new KeyedCodec<>("Sound", Codec.STRING),
                        (e, v) -> e.sound = v, e -> e.sound)
                .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
                .add()
                .append(new KeyedCodec<>("Tint", new EnumCodec<>(Tint.class)),
                        (e, v) -> e.tint = v, e -> e.tint)
                .addValidator(Validators.nonNull())
                .add()
                .append(new KeyedCodec<>("Pulse", ModelParticle.ARRAY_CODEC),
                        (e, v) -> e.pulse = v, e -> e.pulse)
                .add()
                .append(new KeyedCodec<>("Interval", Codec.FLOAT),
                        (e, v) -> e.interval = v, e -> e.interval)
                .addValidator(Validators.greaterThan(0.0f))
                .add()
                .append(new KeyedCodec<>("Trail", ModelAsset.MODEL_TRAIL_CODEC),
                        (e, v) -> e.trail = v, e -> e.trail)
                .add()
                .append(new KeyedCodec<>("Light", Codec.FLOAT),
                        (e, v) -> e.light = v, e -> e.light)
                .addValidator(Validators.range(0.0f, 15.0f))
                .add()
                .build();
    }
}
