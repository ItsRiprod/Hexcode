package com.riprod.hexcode.builtin.statly.interaction;

import org.joml.Vector3d;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.riprod.hexcode.builtin.statly.api.StatlyReactionEvent;
import com.riprod.hexcode.builtin.statly.assets.BaseElementInteraction;
import com.riprod.hexcode.builtin.statly.registry.StatlyReactionContext;
import com.riprod.hexcode.utils.VfxUtil;

public final class GenericElementInteraction extends BaseElementInteraction {

    public static final String ID = "Generic";

    private String particleId;
    private String soundId;

    public static final BuilderCodec<GenericElementInteraction> CODEC = BuilderCodec
            .builder(GenericElementInteraction.class, GenericElementInteraction::new, BaseElementInteraction.BASE_CODEC)
            .append(new KeyedCodec<>("Particle", Codec.STRING, true),
                    (c, v) -> c.particleId = v, c -> c.particleId)
            .addValidatorLate(() -> ParticleSystem.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("Sound", Codec.STRING, true),
                    (c, v) -> c.soundId = v, c -> c.soundId)
            .addValidatorLate(() -> SoundEvent.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void apply(StatlyReactionContext ctx) {
        StatlyReactionEvent event = new StatlyReactionEvent(
                ctx.targetRef(), ctx.elementId(), ctx.attackerCauseId(), ctx.damage(), ctx.hitPosition());
        ctx.accessor().invoke(ctx.targetRef(), event);
        if (event.isCancelled()) {
            return;
        }
        Vector3d pos = ctx.hitPosition();
        if (pos != null) {
            if (particleId != null) VfxUtil.particle(particleId, pos, ctx.accessor());
            if (soundId != null) VfxUtil.sound(soundId, pos, ctx.accessor());
        }
    }
}
