package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;

import com.riprod.hexcode.core.common.execution.impact.Impact;
import com.riprod.hexcode.core.common.glyphs.registry.GlyphConfig;

public final class DomainConfig extends GlyphConfig {

    public static final String ID = "Domain";

    @Nullable
    private Impact triggerImpact;
    @Nullable
    private ModelParticle despawnParticle;
    @Nullable
    private String despawnSound;
    @Nullable
    private ModelParticle contestedParticle;
    @Nullable
    private String contestedSound;

    @Nullable
    public Impact getTriggerImpact() {
        return triggerImpact;
    }

    @Nullable
    public ModelParticle getDespawnParticle() {
        return despawnParticle;
    }

    @Nullable
    public String getDespawnSound() {
        return despawnSound;
    }

    @Nullable
    public ModelParticle getContestedParticle() {
        return contestedParticle;
    }

    @Nullable
    public String getContestedSound() {
        return contestedSound;
    }

    public static final BuilderCodec<DomainConfig> CODEC = BuilderCodec
            .builder(DomainConfig.class, DomainConfig::new, GlyphConfig.BASE_CODEC)
            .append(new KeyedCodec<>("TriggerImpact", Impact.CODEC),
                    (c, v) -> c.triggerImpact = v, c -> c.triggerImpact)
            .add()
            .append(new KeyedCodec<>("DespawnParticle", ModelParticle.CODEC),
                    (c, v) -> c.despawnParticle = v, c -> c.despawnParticle)
            .add()
            .append(new KeyedCodec<>("DespawnSound", Codec.STRING),
                    (c, v) -> c.despawnSound = v, c -> c.despawnSound)
            .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("ContestedParticle", ModelParticle.CODEC),
                    (c, v) -> c.contestedParticle = v, c -> c.contestedParticle)
            .add()
            .append(new KeyedCodec<>("ContestedSound", Codec.STRING),
                    (c, v) -> c.contestedSound = v, c -> c.contestedSound)
            .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();
}
