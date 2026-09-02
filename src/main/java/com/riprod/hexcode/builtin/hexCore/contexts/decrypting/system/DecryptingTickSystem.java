package com.riprod.hexcode.builtin.hexCore.contexts.decrypting.system;

import javax.annotation.Nonnull;

import org.joml.Vector3d;
import org.joml.Vector3f;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hexCore.contexts.decrypting.component.DecryptingState;
import com.riprod.hexcode.builtin.hexCore.nodes.container.ContainerNodeHandler;
import com.riprod.hexcode.builtin.hexCore.obelisks.encryption.EncryptionObelisk;
import com.riprod.hexcode.builtin.hexCore.scene.RadialPositionUtil;
import com.riprod.hexcode.core.common.context.CasterComponent;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.node.component.SlotComponent;
import com.riprod.hexcode.core.common.pedestal.PedestalSlot;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.entity.PedestalEntity;
import com.riprod.hexcode.core.common.pedestal.events.PedestalSystem;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;

public class DecryptingTickSystem extends EntityTickingSystem<EntityStore> {

    @Override
    public Query<EntityStore> getQuery() {
        return DecryptingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        Ref<EntityStore> player = chunk.getReferenceTo(index);
        PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
        HexcodeSessionComponent session = pedestal != null
                ? SessionUtils.resolveSession(pedestal, buffer)
                : null;
        if (pedestal == null || session == null || !session.isOwner(player)) {
            ContextTransitionService.exit(buffer, player, DecryptingState.CONTEXT_ID);
            return;
        }

        CasterComponent caster = chunk.getComponent(index, CasterComponent.getComponentType());
        if (caster != null) {
            caster.consumePrimaryPressed();
            caster.consumePrimaryReleased();
            caster.consumeAbilityPressed();
        }

        String slotKey = EncryptionObelisk.soleSlotKey(session);
        if (slotKey == null) {
            ContextTransitionService.exit(buffer, player, DecryptingState.CONTEXT_ID);
            return;
        }

        if (EncryptionObelisk.sessionLocked(buffer, session)) {
            if (!EncryptionObelisk.bound(pedestal, buffer)) {
                PlayerRef pr = buffer.getComponent(player, PlayerRef.getComponentType());
                if (pr != null) {
                    NotificationUtil.sendNotification(pr.getPacketHandler(),
                            Message.translation("hexcode.components.encrypted.locked"),
                            Message.translation("hexcode.components.encrypted.needObelisk"));
                }
                ContextTransitionService.exit(buffer, player, DecryptingState.CONTEXT_ID);
            }
            return;
        }

        advance(buffer, player, session, slotKey);
    }

    private static void advance(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            HexcodeSessionComponent session, String slotKey) {
        Ref<EntityStore> containerRef = session.getActiveContainerRef();
        if (containerRef == null) {
            spawnContainer(buffer, player, session, slotKey);
            return;
        }
        if (!containerRef.isValid()
                || buffer.getComponent(containerRef, HexComponent.getComponentType()) == null) {
            return;
        }
        ContextTransitionService.transitionFrom(buffer, player,
                DecryptingState.CONTEXT_ID, CraftingState.CONTEXT_ID, CraftingState.PRIORITY);
    }

    private static void spawnContainer(CommandBuffer<EntityStore> buffer, Ref<EntityStore> player,
            HexcodeSessionComponent session, String slotKey) {
        Ref<EntityStore> anchorRef = session.getAnchorRef();
        if (anchorRef == null || !anchorRef.isValid()) {
            return;
        }
        var profile = session.getProfile();
        PedestalSlot slotAsset = profile != null
                ? profile.findSlot(session.getStoredItem(), slotKey) : null;
        if (slotAsset == null) {
            return;
        }
        Vector3d anchorPos = PedestalEntity.getAnchorPosition(session.getPedestalLocation());
        Vector3f offset = RadialPositionUtil.calculateOffsets(1, PedestalSystem.PREVIEW_RADIUS, 0,
                PedestalSystem.HEX_SLOT_OFFSET).get(0);
        Hex hex = session.getHexAt(slotKey, buffer);
        Ref<EntityStore> containerRef = ContainerNodeHandler.INSTANCE.spawnContainer(buffer, hex,
                anchorRef, anchorPos, offset, player, slotAsset);
        if (containerRef == null) {
            return;
        }
        buffer.addComponent(containerRef, SlotComponent.getComponentType(), new SlotComponent(slotKey));
        session.setActiveSlotKey(slotKey);
        session.setActiveContainerRef(containerRef);
    }
}
