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
        var book = bookAsset != null ? bookAsset.getEffects() : null;
        if (book == null) return;

        ctx.mutableEffects().compose(book.palette());
    }
}
