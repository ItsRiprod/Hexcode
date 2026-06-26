package com.riprod.hexcode.builtin.hexCore.obelisks.importexport.interactions;

import javax.annotation.Nonnull;

import org.joml.Vector3i;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.obelisks.importexport.ImportExportPage;
import com.riprod.hexcode.core.common.hexcaster.component.HexcasterComponent;
import com.riprod.hexcode.state.HexState;

public class ImportInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<ImportInteraction> CODEC = BuilderCodec
            .builder(ImportInteraction.class, ImportInteraction::new, SimpleInteraction.CODEC)
            .build();

    public ImportInteraction() {
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

            var targetBlock = ctx.getTargetBlock();
            var targetBlockLocation = new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z);

            HexcasterComponent casterComp = buffer.getComponent(playerRef,
                    HexcasterComponent.getComponentType());

            if (casterComp == null) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            PlayerRef ref = buffer.getComponent(playerRef, PlayerRef.getComponentType());
            if (ref == null)
                return;
            if (casterComp.getState() != HexState.CRAFTING) {
                ref.sendMessage(Message.raw("You must be in Crafting Mode to import/export a hex"));
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            Player player = buffer.getComponent(playerRef, Player.getComponentType());
            if (player == null)
                return;

            Store<EntityStore> store = buffer.getExternalData().getWorld().getEntityStore().getStore();
            player.getPageManager().openCustomPage(playerRef, store, new ImportExportPage(ref, targetBlockLocation));

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