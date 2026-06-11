package com.riprod.hexcode.core.common.drawing.interactions;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** @deprecated - in progress and deferred for the time being */
public class HexDrawMode extends ChargingInteraction {

    @Nonnull
    public static final BuilderCodec<HexDrawMode> CODEC = BuilderCodec
            .builder(HexDrawMode.class, HexDrawMode::new, ChargingInteraction.ABSTRACT_CODEC)
            .appendInherited(new KeyedCodec<>("AllowIndefiniteHold", Codec.BOOLEAN),
                    (i, s) -> i.allowIndefiniteHold = s,
                    i -> i.allowIndefiniteHold,
                    (i, p) -> i.allowIndefiniteHold = p.allowIndefiniteHold)
            .add()
            .build();

    private static final float COMMIT_DELAY = 2.0F;
    private static final float NO_DEADLINE = -1.0F;

    private static final MetaKey<Float> COMMIT_DEADLINE = Interaction.META_REGISTRY.registerMetaObject(i -> NO_DEADLINE);
    private static final MetaKey<Integer> LAST_STROKE_COUNT = Interaction.META_REGISTRY.registerMetaObject(i -> 0);

    public HexDrawMode() {
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Client;
    }

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context,
            @Nonnull CooldownHandler cooldownHandler) {
        InteractionSyncData clientData = context.getClientState();
        DynamicMetaStore<Interaction> store = context.getInstanceStore();

        // HexDraw holds while Primary is down, so the client only bumps forkCounts when a stroke releases;
        // each bump (re)opens the idle window, which a following stroke resets the same way
        if (clientData.forkCounts != null) {
            int strokes = 0;
            for (int count : clientData.forkCounts.values()) {
                strokes += count;
            }
            if (strokes > store.getMetaObject(LAST_STROKE_COUNT)) {
                store.putMetaObject(LAST_STROKE_COUNT, strokes);
                store.putMetaObject(COMMIT_DEADLINE, time + COMMIT_DELAY);
            }
        }

        float deadline = store.getMetaObject(COMMIT_DEADLINE);
        if (deadline != NO_DEADLINE && time >= deadline) {
            store.putMetaObject(COMMIT_DEADLINE, NO_DEADLINE);
            this.fireCommit(context);
        }

        super.tick0(firstRun, time, type, context, cooldownHandler);
    }

    private void fireCommit(@Nonnull InteractionContext context) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        if (commandBuffer == null || playerRef == null || !playerRef.isValid()) {
            return;
        }

        PlayerRef pr = commandBuffer.getComponent(playerRef, PlayerRef.getComponentType());
        if (pr != null) {
            pr.sendMessage(Message.raw("[hexcode] draw committed (2s idle)"));
        }
    }
}
