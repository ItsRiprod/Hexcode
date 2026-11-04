package com.riprod.hexcode.builtin.elementalism.effects;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

public final class EffectEntry {
    public static final BuilderCodec<EffectEntry> CODEC;

    private String particle;
    private String sound;
    private Color color;

    private EffectEntry() {
    }

    EffectEntry(@Nullable String particle, @Nullable String sound, @Nullable Color color) {
        this.particle = particle;
        this.sound = sound;
        this.color = color;
    }

    @Nullable
    public String getParticle() {
        return this.particle;
    }

    @Nullable
    public String getSound() {
        return this.sound;
    }

    @Nullable
    public Color getColor() {
        return this.color;
    }

    static {
        CODEC = BuilderCodec.builder(EffectEntry.class, EffectEntry::new)
                .append(new KeyedCodec<>("Particle", Codec.STRING),
                        (e, v) -> e.particle = v, e -> e.particle)
                .addValidator(ParticleSystem.VALIDATOR_CACHE.getValidator())
                .documentation("Particle system id")
                .add()
                .append(new KeyedCodec<>("Sound", Codec.STRING),
                        (e, v) -> e.sound = v, e -> e.sound)
                .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
                .documentation("Sound event id")
                .add()
                .append(new KeyedCodec<>("Color", ProtocolCodecs.COLOR),
                        (e, v) -> e.color = v, e -> e.color)
                .documentation("Tint applied to the particle")
                .add()
                .build();
    }
}
