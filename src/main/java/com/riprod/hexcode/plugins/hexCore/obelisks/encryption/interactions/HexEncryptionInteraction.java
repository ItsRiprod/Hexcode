package com.riprod.hexcode.plugins.hexCore.obelisks.encryption.interactions;

import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
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
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.obelisk.component.ObeliskBlockComponent;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.profiles.assets.ProfileAsset;
import com.riprod.hexcode.plugins.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.plugins.hexCore.obelisks.encryption.EncryptionObelisk;
import com.riprod.hexcode.plugins.hexCore.obelisks.encryption.EncryptionSessionState;

public class HexEncryptionInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<@NotNull HexEncryptionInteraction> CODEC = BuilderCodec
            .builder(HexEncryptionInteraction.class, HexEncryptionInteraction::new, SimpleInteraction.CODEC)
            .append(new KeyedCodec<>("Action", new EnumCodec<>(EncryptionActionOptions.class)),
                    (value, interaction) -> value.setOption(interaction),
                    interaction -> interaction.getOption())
            .add()
            .build();

    private EncryptionActionOptions option = EncryptionActionOptions.ENCRYPT;

    public HexEncryptionInteraction() {
    }

    public void setOption(EncryptionActionOptions option) {
        this.option = option;
    }

    public EncryptionActionOptions getOption() {
        return option;
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
            Ref<EntityStore> ref = ctx.getEntity();
            if (buffer == null || ref == null || !ref.isValid()) {
                ctx.getState().state = InteractionState.Failed;
                return;
            }

            switch (option) {
                case ENCRYPT -> Encrypt(ctx, buffer, ref);
                case CLEAR -> Clear(ctx, buffer, ref);
            }

            ctx.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, ctx, cooldown);

        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] EncryptionArmInteraction failed: %s", e.getMessage());
            ctx.getState().state = InteractionState.Failed;
        }
    }

    private static void Encrypt(InteractionContext ctx, ComponentAccessor<EntityStore> buffer, Ref<EntityStore> ref) {

        PlayerRef pr = buffer.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null) {
            return;
        }

        CasterComponent casterComp = buffer.getComponent(ref,
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
        ProfileAsset profile = session != null ? session.getProfile() : null;
        if (session == null || profile == null
                || !profile.allowsObelisk(EncryptionObelisk.HANDLER_ID)) {
            pr.sendMessage(Message.translation("hexcode.components.encode.notComponent"));
            ctx.getState().state = InteractionState.Failed;
            return;
        }
        if (!session.isOwner(ref)) {
            pr.sendMessage(Message.translation("hexcode.components.encode.notOwner"));
            ctx.getState().state = InteractionState.Failed;
            return;
        }

        EncryptionSessionState state = session.obeliskState(EncryptionObelisk.HANDLER_ID,
                EncryptionSessionState::new);
        state.setCaptureArmed(true);
        NotificationUtil.sendNotification(pr.getPacketHandler(),
                Message.translation("hexcode.components.encode.armed"),
                Message.translation("hexcode.components.encode.armedDesc"));
    }

    private static void Clear(InteractionContext ctx, ComponentAccessor<EntityStore> buffer, Ref<EntityStore> ref) {
        var heldItem = ctx.getHeldItem();
        if (heldItem == null || heldItem.isEmpty())
            return;

        heldItem.getItem().getData().getTag(1).forEach(tag -> {});

        var imbuedData = ImbuementUtils.read(heldItem);

    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
    }
}
