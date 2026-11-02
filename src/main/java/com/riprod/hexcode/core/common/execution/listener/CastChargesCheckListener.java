package com.riprod.hexcode.core.common.execution.listener;

import java.util.function.Consumer;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.riprod.hexcode.api.event.CastEvent;

public class CastChargesCheckListener implements Consumer<CastEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void accept(CastEvent event) {
        var ctx = event.getContext();
        if (ctx.getCastSlotKey() != null || !ctx.policy().isRequireMagicCharges()) return;
        var accessor = ctx.getAccessor();
        var rootRef = ctx.getRootRef();
        if ((int) CasterStats.maxMagicCharges(rootRef, accessor) > 0) return;
        try {
            var pr = accessor.getComponent(rootRef, PlayerRef.getComponentType());
            if (pr != null) pr.sendMessage(Message.raw("no spell slots available"));
        } catch (Exception e) {
            LOGGER.atWarning().log("CastChargesCheckListener noSlots message failed: %s", e.getMessage());
        }
        event.setCancelled(true);
    }
}
