package com.riprod.hexcode.core.common.execution.listener;

import java.util.function.Consumer;

import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexcaster.utils.PlayerUtils;
import com.riprod.hexcode.utils.HexSlot;

public class CastBookTintListener implements Consumer<CastEvent> {

    @Override
    public void accept(CastEvent event) {
        var ctx = event.getContext();
        var bookAsset = CasterInventory.getHexBookAsset(
                PlayerUtils.getHandItem(ctx.getAccessor(), ctx.getRootRef(), HexSlot.OffHand));
        if (bookAsset == null) return;

        if (bookAsset.getStyle() != null && bookAsset.getStyle().getSecondaryColor() != null) {
            ctx.mutableStyle().setSecondaryColor(bookAsset.getStyle().getSecondaryColor().clone());
        }
    }
}
