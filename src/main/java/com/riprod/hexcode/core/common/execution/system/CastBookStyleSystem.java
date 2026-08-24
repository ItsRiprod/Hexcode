package com.riprod.hexcode.core.common.execution.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.config.HexConfigAsset;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.root.PlayerHexRoot;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.utils.HexSlot;

public class CastBookStyleSystem extends WorldEventSystem<EntityStore, HexCastEvent.Pre> {

    public CastBookStyleSystem() {
        super(HexCastEvent.Pre.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexCastEvent.Pre event) {
        if (event.isCancelled()) return;
        HexContext context = event.getContext();
        if (!(context.getHexRoot() instanceof PlayerHexRoot playerRoot)) return;
        Ref<EntityStore> casterRef = playerRoot.getSourceRef(buffer);
        if (casterRef == null || !casterRef.isValid()) return;

        HexBookAsset bookAsset = CasterInventory.getHexBookAsset(
                PlayerUtils.getHandItem(buffer, casterRef, HexSlot.OffHand));
        if (bookAsset == null) return;

        if (context.getStyle() != null && bookAsset.getStyle() != null
                && bookAsset.getStyle().getSecondaryColor() != null) {
            context.mutableStyle().setSecondaryColor(bookAsset.getStyle().getSecondaryColor().clone());
        }
        HexConfigAsset defaults = bookAsset.getDefaults();
        if (defaults != null) defaults.applyTo(context);
    }
}
