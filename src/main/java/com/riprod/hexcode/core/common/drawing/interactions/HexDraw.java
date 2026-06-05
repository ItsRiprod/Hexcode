package com.riprod.hexcode.core.common.drawing.interactions;

import java.util.Arrays;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.drawing.DrawingSystem;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.state.HexState;
import com.riprod.hexcode.state.HexcodeManager;
import com.riprod.hexcode.state.StateRouter;

import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;

public class HexDraw extends ChargingInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // @Nonnull
    // public static final BuilderCodec<HexDraw> CODEC = BuilderCodec
    //         .builder(HexDraw.class, HexDraw::new, ChargingInteraction.ABSTRACT_CODEC)
    //         .<String>appendInherited(
    //                 new KeyedCodec<>("Next", Interaction.CHILD_ASSET_CODEC),
    //                 (i, s) -> {
    //                     i.next = new Float2ObjectOpenHashMap<>();
    //                     i.next.put(0.0f, s);
    //                 },
    //                 i -> i.next != null ? i.next.get(0.0f) : null,
    //                 (i, p) -> i.next = p.next)
    //         .add()
    //         .afterDecode(i -> {
    //             i.allowIndefiniteHold = true;
    //             if (i.next != null) {
    //                 i.sortedKeys = i.next.keySet().toFloatArray();
    //                 Arrays.sort(i.sortedKeys);
    //                 i.highestChargeValue = i.sortedKeys[i.sortedKeys.length - 1];
    //             }
    //         })
    //         .build();

    public HexDraw() {
    }

    @Nonnull
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Client;
    }

    @Override
    protected void tick0(boolean firstRun, float dt, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
        try {

            CommandBuffer<EntityStore> commandBuffer = ctx.getCommandBuffer();
            if (commandBuffer == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            Ref<EntityStore> playerRef = ctx.getOwningEntity();
            if (playerRef == null || !playerRef.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            HexcasterComponent hexcaster = commandBuffer.getComponent(playerRef, HexcasterComponent.getComponentType());
            if (hexcaster == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            if (firstRun) {
                ctx.getState().state = DrawingSystem.enterInteraction(commandBuffer, playerRef, hexcaster);
            } else {
                ctx.getState().state = DrawingSystem.tickInteraction(commandBuffer, playerRef, dt, hexcaster);
            }

            super.tick0(firstRun, dt, type, ctx, cooldown);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] DrawInteraction failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }
}
