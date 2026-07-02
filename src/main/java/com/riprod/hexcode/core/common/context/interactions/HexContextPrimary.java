package com.riprod.hexcode.core.common.context.interactions;

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
import com.riprod.hexcode.core.common.context.CasterComponent;

public class HexContextPrimary extends ChargingInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexContextPrimary> CODEC = BuilderCodec
            .builder(HexContextPrimary.class, HexContextPrimary::new, ChargingInteraction.CODEC)
            .build();

    public HexContextPrimary() {
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

            CasterComponent caster = buffer.getComponent(player, CasterComponent.getComponentType());
            if (caster == null || caster.getCurrentContext() == null) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            if (firstRun) {
                caster.beginPrimary();
            } else {
                caster.tickPrimary();
            }

            super.tick0(firstRun, time, type, ctx, cooldown);

            if (ctx.getState().state != InteractionState.NotFinished) {
                caster.endPrimary();
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexContextPrimary failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }
}
