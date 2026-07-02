package com.riprod.hexcode.core.common.drawing.interactions;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.drawing.DrawCaptureService;
import com.riprod.hexcode.core.common.drawing.component.DrawCaptureComponent;

public class HexDraw extends ChargingInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexDraw> CODEC = BuilderCodec
            .builder(HexDraw.class, HexDraw::new, ChargingInteraction.CODEC)
            .build();

    public HexDraw() {
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Client;
    }

    @Override
    protected void tick0(boolean firstRun, float dt, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
        try {
            CommandBuffer<EntityStore> buffer = ctx.getCommandBuffer();
            Ref<EntityStore> player = ctx.getOwningEntity();
            if (buffer == null || player == null || !player.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            DrawCaptureComponent capture = buffer.getComponent(player, DrawCaptureComponent.getComponentType());
            if (capture == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            HeadRotation head = buffer.getComponent(player, HeadRotation.getComponentType());
            if (head == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            if (firstRun) {
                // server-reinterpretation: hovering a hex turns this press into a drag,
                // otherwise it is a stroke; the active context performs the drag itself
                if (capture.getHoveredHex() != null) {
                    capture.setDraggingHex(capture.getHoveredHex());
                } else {
                    DrawCaptureService.beginStroke(buffer, player, capture, head);
                }
            } else if (capture.isStrokeActive()) {
                DrawCaptureService.tickStroke(buffer, player, capture, head);
            }

            super.tick0(firstRun, dt, type, ctx, cooldown);

            if (ctx.getState().state != InteractionState.NotFinished) {
                if (capture.getDraggingHex() != null) {
                    capture.requestDragRelease();
                } else if (capture.isStrokeActive()) {
                    DrawCaptureService.endStroke(buffer, player, capture, resolveUuid(buffer, player));
                }
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] HexDraw failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }

    @Nullable
    private static UUID resolveUuid(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        UUIDComponent uuid = buffer.getComponent(player, UUIDComponent.getComponentType());
        return uuid != null ? uuid.getUuid() : null;
    }
}
