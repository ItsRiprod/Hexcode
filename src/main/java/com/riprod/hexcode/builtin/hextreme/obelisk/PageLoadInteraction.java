package com.riprod.hexcode.builtin.hextreme.obelisk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.riprod.hexcode.builtin.hextreme.execution.config.PageConfig;
import com.riprod.hexcode.builtin.hextreme.imbuement.PageProfile;
import com.riprod.hexcode.builtin.hextreme.imbuement.PageStateResolver;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.utils.HexSlot;
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
                notify(ref, "hexcode.pages.obelisk.not_crafting");
                return InteractionState.Failed;
            }

            HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(playerRef, buffer);
            if (session == null) {
                return InteractionState.Failed;
            }

            Ref<EntityStore> ownerRef = session.getOwnerRef();
            if (ownerRef == null || !ownerRef.isValid() || !ownerRef.equals(playerRef)) {
                notify(ref, "hexcode.pages.obelisk.not_owner");
                return InteractionState.Failed;
            }

            if (session.getActiveSlotKey() == null) {
                notify(ref, "hexcode.pages.obelisk.no_slot");
                return InteractionState.Failed;
            }

            ItemStack held = ctx.getHeldItem();

            Hex hex = PageConfig.resolvePageHex(buffer, held);
            if (hex != null) {
                session.setPendingImportHex(hex);
                spendPage(buffer, playerRef, held);
                return InteractionState.Finished;
            }

            if (isBlankPage(held)) {
                session.setPendingExportPage(held);
                return InteractionState.Finished;
            }

            notify(ref, "hexcode.pages.load.no_hex");
            return InteractionState.Failed;
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode] PageLoadInteraction failed: %s", e.getMessage());
            return InteractionState.Failed;
        }
    }

    private static void spendPage(CommandBuffer<EntityStore> buffer, Ref<EntityStore> playerRef, ItemStack page) {
        if (ImbuementUtils.resolveProfile(page) instanceof PageProfile profile) {
            PlayerUtils.setHandItem(buffer, playerRef, HexSlot.MainHand,
                    profile.writeHex(page, profile.getSlotKey(), null));
        }
    }

    private static boolean isBlankPage(ItemStack item) {
        return ImbuementUtils.resolveProfile(item) instanceof PageProfile profile
                && PageStateResolver.resolveBase(item, profile.getEmptyStateKey()) != null;
    }

    private static void notify(@Nullable PlayerRef ref, String key) {
        if (ref != null) {
            ref.sendMessage(Message.translation(key));
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type,
            @Nonnull InteractionContext ctx, @Nonnull CooldownHandler cooldown) {
    }
}
