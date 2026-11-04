package com.riprod.hexcode.core.common.execution.listener;

import java.util.function.Consumer;

import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.execution.component.CasterStateComponent;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;

public class CastChargesApplyListener implements Consumer<CastEvent> {

    @Override
    public void accept(CastEvent event) {
        var ctx = event.getContext();
        if (!ctx.policy().isRequireMagicCharges()) return;
        var accessor = ctx.getAccessor();
        var rootRef = ctx.getRootRef();

        var casterState = accessor.ensureAndGetComponent(rootRef, CasterStateComponent.getComponentType());
        var casts = accessor.getResource(HexCastStore.getResourceType());
        var slotKey = ctx.getCastSlotKey();

        if (slotKey == null) {
            int max = (int) CasterStats.maxMagicCharges(rootRef, accessor);
            while (casterState.getActiveCount(casts) >= max) {
                casterState.evictOldest(casts, accessor);
            }
        } else {
            casterState.fizzleSlot(casts, slotKey, accessor);
        }

        casterState.registerActiveCast(ctx.cast());
    }
}
