package com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.api.context.HexContextChangeEvent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.system.GravityUtil;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.component.DecryptingState;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionObelisk;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;

public class DecryptingChangeListener extends WorldEventSystem<EntityStore, HexContextChangeEvent> {

    public DecryptingChangeListener() {
        super(HexContextChangeEvent.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexContextChangeEvent event) {
        Ref<EntityStore> player = event.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }

        if (DecryptingState.CONTEXT_ID.equals(event.getNewContextId())) {
            enter(buffer, player);
            return;
        }

        DecryptingState state = buffer.getComponent(player, DecryptingState.getComponentType());
        if (state == null) {
            return;
        }
        buffer.tryRemoveComponent(player, DecryptingState.getComponentType());

        if (CraftingState.CONTEXT_ID.equals(event.getNewContextId())) {
            return;
        }
        ContextTransitionService.setInContextStat(buffer, player, false);
        GravityUtil.exitFly(buffer, player);
        endSessionIfOwner(buffer, player);
    }

    private static void enter(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        buffer.putComponent(player, DecryptingState.getComponentType(), new DecryptingState());
        ContextTransitionService.setInContextStat(buffer, player, true);
        GravityUtil.enterFly(buffer, player);

        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        HexcodeSessionComponent session = pedestal != null
                ? SessionUtils.resolveSession(pedestal, buffer)
                : null;
        if (pedestal == null || session == null || !session.isOwner(player)) {
            return;
        }

        World world = buffer.getExternalData().getWorld();
        ImbuementProfileAsset profile = session.getProfile();
        PedestalSystem.registerObelisks(buffer, world, pedestal, profile);

        PedestalSystem.updateState(buffer, pedestal, session, world, PedestalState.SELECTING);

        if (EncryptionObelisk.sessionLocked(buffer, session)) {
            PlayerRef pr = buffer.getComponent(player, PlayerRef.getComponentType());
            if (pr != null) {
                NotificationUtil.sendNotification(pr.getPacketHandler(),
                        Message.translation("hexcode.components.encrypted.locked"),
                        Message.translation("hexcode.components.encrypted.drawPrompt"));
            }
        }
    }

    private static void endSessionIfOwner(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player) {
        Ref<EntityStore> sessionRef = SessionUtils.getSessionRefByPlayer(player, buffer);
        HexcodeSessionComponent session = sessionRef != null
                ? buffer.getComponent(sessionRef, HexcodeSessionComponent.getComponentType())
                : null;
        if (session != null && session.isOwner(player)) {
            SessionUtils.endSession(buffer, sessionRef, buffer.getExternalData().getWorld());
        }
    }
}
