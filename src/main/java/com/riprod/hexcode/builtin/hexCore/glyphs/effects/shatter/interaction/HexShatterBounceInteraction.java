package com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector4d;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.component.ShatterState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.style.ShatterStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;

public class HexShatterBounceInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexShatterBounceInteraction> CODEC = BuilderCodec
            .builder(HexShatterBounceInteraction.class, HexShatterBounceInteraction::new, SimpleInteraction.CODEC)
            .build();

    public HexShatterBounceInteraction() {
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
            Ref<EntityStore> shardRef = ctx.getEntity();
            if (buffer == null || shardRef == null || !shardRef.isValid()) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            ShatterState state = buffer.getComponent(shardRef, ShatterState.getComponentType());
            HexContext hexContext = state != null ? state.getHexContext() : null;

            Vector4d hitLocation = ctx.getMetaStore().getMetaObject(Interaction.HIT_LOCATION);
            if (hitLocation != null && hexContext != null) {
                Vector3d hitPos = new Vector3d(hitLocation.x, hitLocation.y, hitLocation.z);
                ShatterStyle.renderShardHit(hitPos, hexContext, buffer);
            }

            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] HexShatterBounceInteraction failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext ctx) {
        return false;
    }
}
