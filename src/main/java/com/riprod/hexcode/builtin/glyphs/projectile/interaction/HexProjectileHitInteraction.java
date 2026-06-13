package com.riprod.hexcode.builtin.glyphs.projectile.interaction;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector4d;
import com.hypixel.hytale.protocol.BlockPosition;
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
import com.riprod.hexcode.builtin.glyphs.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.glyphs.projectile.style.ProjectileStyle;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class HexProjectileHitInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexProjectileHitInteraction> CODEC = BuilderCodec
            .builder(HexProjectileHitInteraction.class, HexProjectileHitInteraction::new, SimpleInteraction.CODEC)
            .build();

    public HexProjectileHitInteraction() {
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
            Ref<EntityStore> projectileRef = ctx.getEntity();
            if (buffer == null || projectileRef == null || !projectileRef.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            ProjectileState state = buffer.getComponent(projectileRef, ProjectileState.getComponentType());
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
                if (hitPos != null) {
                    ProjectileStyle.renderEntityHit(hitPos, hitPos, hexContext, buffer);
                }
            } else if (hitPos != null) {
                BlockPosition hitBlock = ctx.getMetaStore().getMetaObject(Interaction.TARGET_BLOCK_RAW);
                resultVar = hitBlock != null
                        ? new BlockVar(new Vector3i(hitBlock.x, hitBlock.y, hitBlock.z))
                        : new PositionVar(hitPos, true);
                ProjectileStyle.renderBlockHit(hitPos, hexContext, buffer);
            }

            if (triggering != null && resultVar != null) {
                triggering.writeOutput(resultVar, hexContext);
            }

            HexExecuter.continueExecution(state.getNextLinks(), hexContext);

            buffer.tryRemoveEntity(projectileRef, RemoveReason.REMOVE);

            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] HexProjectileHitInteraction failed: %s", e.getMessage());
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
