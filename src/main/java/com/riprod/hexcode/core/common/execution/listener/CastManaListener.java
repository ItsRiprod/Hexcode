package com.riprod.hexcode.core.common.execution.listener;

import java.util.function.Consumer;

import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.cast.component.ManaSourceComponent;

public class CastManaListener implements Consumer<CastEvent> {

    @Override
    public void accept(CastEvent event) {
        var ctx = event.getContext();
        if (!ctx.policy().isConsumeMana()) return;
        if (ctx.get(ManaSourceComponent.TYPE).tryConsume(ctx.getManaCost(), ctx.getAccessor())) return;
        HexExecuter.fail(null, ctx, GlyphFizzleEvent.Reason.INSUFFICIENT_MANA);
        event.setCancelled(true);
    }
}
