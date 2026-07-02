package com.riprod.hexcode.interaction;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.events.PedestalInteractEvent;
import com.hypixel.hytale.logger.HytaleLogger;

import org.joml.Vector3i;

public class PedestalInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<PedestalInteraction> CODEC = BuilderCodec
            .builder(PedestalInteraction.class, PedestalInteraction::new, SimpleInteraction.CODEC)
            .build();

    public PedestalInteraction() {
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
            if (buffer == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            Ref<EntityStore> playerRef = ctx.getEntity();
            if (playerRef == null || !playerRef.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            BlockPosition targetBlock = ctx.getTargetBlock();
            if (targetBlock == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            Vector3i blockPos = new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z);
            World world = buffer.getExternalData().getWorld();
            PedestalBlockComponent pedestal = BlockModule.getComponent(
                    PedestalBlockComponent.getComponentType(), world,
                    blockPos.x, blockPos.y, blockPos.z);
            if (pedestal == null) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            HytaleServer.get().getEventBus().dispatchFor(PedestalInteractEvent.class)
                    .dispatch(new PedestalInteractEvent(buffer, playerRef, pedestal, blockPos));

            ctx.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, ctx, cooldown);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] PedestalInteraction failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
    }
}
