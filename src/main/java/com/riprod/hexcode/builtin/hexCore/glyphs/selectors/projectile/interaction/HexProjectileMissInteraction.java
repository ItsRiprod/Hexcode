package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.interaction;

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
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.glyphs.utils.BlockResolution;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.hexCore.glyphs.selectors.projectile.style.ProjectileStyle;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;

public class HexProjectileMissInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexProjectileMissInteraction> CODEC = BuilderCodec
            .builder(HexProjectileMissInteraction.class, HexProjectileMissInteraction::new, SimpleInteraction.CODEC)
            .build();

    public HexProjectileMissInteraction() {
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

            Vector4d hitLocation = ctx.getMetaStore().getMetaObject(Interaction.HIT_LOCATION);
            Vector3d hitPos = hitLocation != null
                    ? new Vector3d(hitLocation.x, hitLocation.y, hitLocation.z)
                    : null;

            Glyph triggering = state.getTriggeringGlyph();
            if (hitPos != null) {
                if (triggering != null) {
                    Velocity projVel = buffer.getComponent(projectileRef, Velocity.getComponentType());
                    Vector3d travelDir = projVel != null && projVel.getVelocity().lengthSquared() > 1e-6
                            ? new Vector3d(projVel.getVelocity()) : null;
                    Vector3i snapped = BlockResolution.snapIntoBlock(
                            buffer.getExternalData().getWorld(), hitPos, travelDir);
                    triggering.writeOutput(snapped != null
                            ? new BlockVar(snapped)
                            : new PositionVar(hitPos, true), hexContext);
                }
                ProjectileStyle.renderBlockHit(hitPos, hexContext, buffer);
            }

            HexExecuter.continueExecution(state.getNextLinks(), hexContext);

            buffer.tryRemoveEntity(projectileRef, RemoveReason.REMOVE);

            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] HexProjectileMissInteraction failed: %s", e.getMessage());
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
