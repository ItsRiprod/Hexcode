package com.riprod.hexcode.core.common.execution.interactions;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.component.HexConfigAsset;

public class HexHold extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private boolean holdingPrimary = false;

    @Nonnull
    public static final BuilderCodec<HexHold> CODEC = BuilderCodec
            .builder(HexHold.class, HexHold::new, SimpleInteraction.CODEC)
            .<Boolean>appendInherited(
                    new KeyedCodec<>("HoldState", Codec.BOOLEAN, true),
                    (i, v) -> i.holdingPrimary = v,
                    i -> i.holdingPrimary,
                    (i, p) -> i.holdingPrimary = p.holdingPrimary)
            .add()
            .build();

    public HexHold() {
    }

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
        try {
            if (!firstRun) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            CommandBuffer<EntityStore> buffer = ctx.getCommandBuffer();
            Ref<EntityStore> player = ctx.getEntity();
            if (buffer == null || player == null || !player.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            CasterStateComponent casterState = buffer.getComponent(player,
                    CasterStateComponent.getComponentType());

            if (casterState != null) {
                casterState.setHoldingPrimary(holdingPrimary);
            }

            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexHold failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
    }
}
