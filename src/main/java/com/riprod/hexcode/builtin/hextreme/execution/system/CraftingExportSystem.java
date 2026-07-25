package com.riprod.hexcode.builtin.hextreme.execution.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.component.CraftingState;
import com.riprod.hexcode.builtin.hextreme.imbuement.PageProfile;
import com.riprod.hexcode.builtin.hexCore.contexts.selecting.component.SelectingState;
import com.riprod.hexcode.core.common.context.ContextTransitionService;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.pedestal.component.PedestalBlockComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.core.common.pedestal.utils.PedestalBlockUtil;
import com.riprod.hexcode.utils.HexSlot;

public class CraftingExportSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return CraftingState.getComponentType();
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        try {
            Ref<EntityStore> player = chunk.getReferenceTo(index);
            PedestalBlockComponent pedestal = PedestalBlockUtil.resolvePedestal(player, buffer);
            if (pedestal == null) {
                return;
            }
            HexcodeSessionComponent session = SessionUtils.resolveSession(pedestal, buffer);
            if (session == null || session.getPendingExportPage() == null) {
                return;
            }

            ItemStack page = session.getPendingExportPage();
            session.setPendingExportPage(null);

            String slotKey = session.getActiveSlotKey();
            Hex hex = readActiveHex(buffer, session);
            if (hex == null || slotKey == null) {
                return;
            }

            ItemStack inscribed = inscribe(page, hex);
            if (inscribed == null) {
                return;
            }
            PlayerUtils.setHandItem(buffer, player, HexSlot.MainHand, inscribed);

            // teardown's saveHexToBook bails on a null activeSlotKey, so the source slot is
            // cleared here rather than by letting an emptied hex fall through to it
            ImbuementProfileAsset storedProfile = session.getProfile();
            if (storedProfile != null) {
                session.setStoredItem(storedProfile.writeHex(session.getStoredItem(), slotKey, null));
            }
            session.setActiveSlotKey(null);

            PlayerRef playerRef = buffer.getComponent(player, PlayerRef.getComponentType());
            if (playerRef != null) {
                playerRef.sendMessage(Message.translation("hexcode.pages.write.success"));
            }

            ContextTransitionService.transitionFrom(buffer, player,
                    CraftingState.CONTEXT_ID, SelectingState.CONTEXT_ID, SelectingState.PRIORITY);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode] crafting export failed");
        }
    }

    private static Hex readActiveHex(CommandBuffer<EntityStore> buffer, HexcodeSessionComponent session) {
        Ref<EntityStore> containerRef = session.getActiveContainerRef();
        if (containerRef == null || !containerRef.isValid()) {
            return null;
        }
        HexComponent hexComp = buffer.getComponent(containerRef, HexComponent.getComponentType());
        if (hexComp == null || hexComp.getHex() == null || hexComp.getHex().getGlyphs().isEmpty()) {
            return null;
        }
        Hex hex = hexComp.getHex().clone();
        HexUtils.compress(hex);
        return hex;
    }

    private static ItemStack inscribe(ItemStack page, Hex hex) {
        if (!(ImbuementUtils.resolveProfile(page) instanceof PageProfile profile)) {
            return null;
        }
        String slotKey = profile.getSlotKey();
        return slotKey != null ? profile.writeHex(page, slotKey, hex) : null;
    }
}
