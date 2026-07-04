package com.riprod.hexcode.builtin.hextreme.obelisk;

import javax.annotation.Nonnull;

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
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hextreme.execution.config.PageConfig;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class PageLoadInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<PageLoadInteraction> CODEC = BuilderCodec
            .builder(PageLoadInteraction.class, PageLoadInteraction::new, SimpleInteraction.CODEC)
            .build();

    public PageLoadInteraction() {
    }

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
        ctx.getState().state = firstRun ? resolve(ctx) : InteractionState.Finished;
        // delegate to SimpleInteraction.tick0 so a Failed result jumps past the Next
        // (consume) branch instead of falling through into it
        super.tick0(firstRun, time, type, ctx, cooldown);
    }

    private InteractionState resolve(@Nonnull InteractionContext ctx) {
        try {
            CommandBuffer<EntityStore> buffer = ctx.getCommandBuffer();
            Ref<EntityStore> playerRef = ctx.getEntity();
            if (buffer == null || playerRef == null || !playerRef.isValid()) {
                return InteractionState.Failed;
            }

            PlayerRef ref = buffer.getComponent(playerRef, PlayerRef.getComponentType());

            CasterComponent caster = buffer.getComponent(playerRef, CasterComponent.getComponentType());
            if (caster == null || !CraftingState.CONTEXT_ID.equals(caster.getCurrentContext())) {
                if (ref != null) ref.sendMessage(Message.raw("You must be in Crafting Mode to load a page"));
                return InteractionState.Failed;
            }

            HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(playerRef, buffer);
            if (session == null) {
                return InteractionState.Failed;
            }

            Ref<EntityStore> ownerRef = session.getOwnerRef();
            if (ownerRef == null || !ownerRef.isValid() || !ownerRef.equals(playerRef)) {
                if (ref != null) ref.sendMessage(Message.raw("You don't own this pedestal"));
                return InteractionState.Failed;
            }

            if (session.getActiveSlotKey() == null) {
                if (ref != null) ref.sendMessage(Message.raw("Select a slot first"));
                return InteractionState.Failed;
            }

            Hex hex = PageConfig.resolvePageHex(ctx.getHeldItem());
            if (hex == null) {
                if (ref != null) ref.sendMessage(Message.raw("Hold a page inscribed with a spell"));
                return InteractionState.Failed;
            }

            session.setPendingImportHex(hex);
            return InteractionState.Finished;
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] PageLoadInteraction failed: %s", e.getMessage());
            return InteractionState.Failed;
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
    }
}
