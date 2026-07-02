package com.riprod.hexcode.core.common.drawing.interactions;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;

public class HexDrawMode extends ChargingInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexDrawMode> CODEC = BuilderCodec
            .builder(HexDrawMode.class, HexDrawMode::new, ChargingInteraction.CODEC)
            .build();

    public HexDrawMode() {
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Client;
    }

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
        try {
            CommandBuffer<EntityStore> buffer = ctx.getCommandBuffer();
            Ref<EntityStore> player = ctx.getOwningEntity();
            if (buffer == null || player == null || !player.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            // presence of the capture component is the draw flag; the lifecycle refsystem
            // fires enter/exit off these structural writes
            if (firstRun && buffer.getComponent(player, DrawCaptureComponent.getComponentType()) == null) {
                buffer.putComponent(player, DrawCaptureComponent.getComponentType(), new DrawCaptureComponent());
            }

            super.tick0(firstRun, time, type, ctx, cooldown);

            if (ctx.getState().state != InteractionState.NotFinished) {
                buffer.tryRemoveComponent(player, DrawCaptureComponent.getComponentType());
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexDrawMode failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }
}
