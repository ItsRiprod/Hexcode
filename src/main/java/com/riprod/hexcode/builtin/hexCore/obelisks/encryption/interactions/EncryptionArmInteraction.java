package com.riprod.hexcode.builtin.hexCore.obelisks.encryption.interactions;

import javax.annotation.Nonnull;

import org.joml.Vector3i;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionObelisk;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionSessionState;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class EncryptionArmInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<EncryptionArmInteraction> CODEC = BuilderCodec
            .builder(EncryptionArmInteraction.class, EncryptionArmInteraction::new, SimpleInteraction.CODEC)
            .build();

    public EncryptionArmInteraction() {
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
            Ref<EntityStore> playerRef = ctx.getEntity();
            if (buffer == null || playerRef == null || !playerRef.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            PlayerRef pr = buffer.getComponent(playerRef, PlayerRef.getComponentType());
            if (pr == null) {
                return;
            }

            CasterComponent casterComp = buffer.getComponent(playerRef,
                    CasterComponent.getComponentType());
            if (casterComp == null || !CraftingState.CONTEXT_ID.equals(casterComp.getCurrentContext())) {
                pr.sendMessage(Message.translation("hexcode.components.encode.mustCraft"));
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            var targetBlock = ctx.getTargetBlock();
            World world = buffer.getExternalData().getWorld();
            ObeliskBlockComponent obelisk = BlockModule.getComponent(
                    ObeliskBlockComponent.getComponentType(), world,
                    targetBlock.x, targetBlock.y, targetBlock.z);
            Vector3i pedestalLoc = obelisk != null ? obelisk.getRegisteredPedestalLoc() : null;
            PedestalBlockComponent pedestal = pedestalLoc != null
                    ? BlockModule.getComponent(PedestalBlockComponent.getComponentType(), world,
                            pedestalLoc.x, pedestalLoc.y, pedestalLoc.z)
                    : null;
            HexcodeSessionComponent session = pedestal != null
                    ? SessionUtils.resolveSession(pedestal, buffer)
                    : null;
            ImbuementProfileAsset profile = session != null ? session.getProfile() : null;
            if (session == null || profile == null
                    || !profile.allowsObelisk(EncryptionObelisk.HANDLER_ID)) {
                pr.sendMessage(Message.translation("hexcode.components.encode.notComponent"));
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            EncryptionSessionState state = session.obeliskState(EncryptionObelisk.HANDLER_ID,
                    EncryptionSessionState::new);
            state.setCaptureArmed(true);
            NotificationUtil.sendNotification(pr.getPacketHandler(),
                    Message.translation("hexcode.components.encode.armed"),
                    Message.translation("hexcode.components.encode.armedDesc"));

            ctx.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, ctx, cooldown);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] EncryptionArmInteraction failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
    }
}
