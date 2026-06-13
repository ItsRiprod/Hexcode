package com.riprod.hexcode.builtin.glyphs.projectile.interaction;

import java.util.Arrays;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector4d;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.glyphs.projectile.ProjectileGlyphSlots;
import com.riprod.hexcode.builtin.glyphs.projectile.component.ProjectileState;
import com.riprod.hexcode.builtin.glyphs.projectile.style.ProjectileStyle;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.component.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.glyphs.variables.BlockVar;
import com.riprod.hexcode.core.common.glyphs.variables.PositionVar;
import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class HexProjectileBounceInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<HexProjectileBounceInteraction> CODEC = BuilderCodec
            .builder(HexProjectileBounceInteraction.class, HexProjectileBounceInteraction::new,
                    SimpleInteraction.CODEC)
            .build();

    public HexProjectileBounceInteraction() {
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
            if (buffer == null || !projectileRef.isValid()) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            ProjectileState state = buffer.getComponent(projectileRef, ProjectileState.getComponentType());
            HexContext hexContext = state != null ? state.getHexContext() : null;

            Vector4d hitLocation = ctx.getMetaStore().getMetaObject(Interaction.HIT_LOCATION);
            if (hitLocation == null || hexContext == null) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }
            Vector3d hitPos = new Vector3d(hitLocation.x, hitLocation.y, hitLocation.z);
            BlockPosition hitBlock = ctx.getMetaStore().getMetaObject(Interaction.TARGET_BLOCK_RAW);
            HexVar resultVar = hitBlock != null
                    ? new BlockVar(new Vector3i(hitBlock.x, hitBlock.y, hitBlock.z))
                    : new PositionVar(hitPos, true);
            Glyph triggering = state.getTriggeringGlyph();

            if (triggering == null) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }
            Slot immediate = triggering.getSlot(ProjectileGlyphSlots.BOUNCE);
            if (immediate == null)
                return;
            String[] links = immediate.getLinks();
            if (links == null || links.length == 0) {
                ctx.getState().state = InteractionState.Finished;
                return;
            }

            hexContext.updateRuntimeAccessors(buffer);

            ProjectileStyle.renderBlockHit(hitPos, hexContext, buffer);

            triggering.writeOutput(resultVar, hexContext);

            HexExecuter.continueExecution(Arrays.asList(links), hexContext);

            ctx.getState().state = InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] HexProjectileBounceInteraction failed: %s", e.getMessage());
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
