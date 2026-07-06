package com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.interaction;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector4d;

import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.component.ShatterState;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.shatter.style.ShatterStyle;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class HexShatterHitInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexShatterHitInteraction> CODEC = BuilderCodec
            .builder(HexShatterHitInteraction.class, HexShatterHitInteraction::new, SimpleInteraction.CODEC)
            .build();

    public HexShatterHitInteraction() {
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
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            ShatterState state = buffer.getComponent(shardRef, ShatterState.getComponentType());
            if (state == null || state.getHexContext() == null) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            HexContext hexContext = state.getHexContext();
            hexContext.updateRuntimeAccessors(buffer);

            Ref<EntityStore> targetRef = ctx.getMetaStore().getMetaObject(Interaction.TARGET_ENTITY);
            Vector4d hitLocation = ctx.getMetaStore().getMetaObject(Interaction.HIT_LOCATION);
            Vector3d hitPos = hitLocation != null
                    ? new Vector3d(hitLocation.x, hitLocation.y, hitLocation.z)
                    : null;

            Glyph triggering = state.getTriggeringGlyph();
            HexVar resultVar = null;
            if (targetRef != null && targetRef.isValid()) {
                UUIDComponent uuid = buffer.getComponent(targetRef, UUIDComponent.getComponentType());
                if (uuid != null) {
                    resultVar = new EntityVar(uuid.getUuid(), targetRef);
                }
            } else if (hitPos != null) {
                resultVar = new BlockVar(new Vector3i((int) Math.floor(hitPos.x), (int) Math.floor(hitPos.y), (int) Math.floor(hitPos.z)));
            }

            if (hitPos != null) {
                ShatterStyle.renderShardHit(hitPos, hexContext, buffer);
            }

            if (triggering != null && resultVar != null) {
                triggering.writeOutput(resultVar, hexContext);
            }

            HexExecuter.continueExecution(state.getNextLinks(), hexContext);

            buffer.tryRemoveEntity(shardRef, RemoveReason.REMOVE);

            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] HexShatterHitInteraction failed: %s", e.getMessage());
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
