package com.riprod.hexcode.core.common.execution.interactions;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;

public class HexCastHoldInteraction extends ChargingInteraction {

    @Nonnull
    public static final BuilderCodec<HexCastHoldInteraction> CODEC = BuilderCodec
            .builder(HexCastHoldInteraction.class, HexCastHoldInteraction::new, ChargingInteraction.CODEC)
            .build();

    public HexCastHoldInteraction() {
    }
}
