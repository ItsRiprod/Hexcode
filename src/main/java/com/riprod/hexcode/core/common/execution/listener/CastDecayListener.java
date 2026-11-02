package com.riprod.hexcode.core.common.execution.listener;

import java.util.function.Consumer;

import com.riprod.hexcode.api.event.CastEvent;
import com.riprod.hexcode.core.common.execution.component.ExecutionComponent;

public class CastDecayListener implements Consumer<CastEvent> {

    @Override
    public void accept(CastEvent event) {
        var ctx = event.getContext();
        if (!ctx.policy().isApplyVolatilityDecay()) return;
        if (ctx.getCastSlotKey() != null) return;
        var accessor = ctx.getAccessor();
        var rootRef = ctx.getRootRef();
        var tracker = ctx.volatility();

        var exec = accessor.ensureAndGetComponent(rootRef, ExecutionComponent.getComponentType());

        float stability = Math.max(0f, Math.min(100f, CasterStats.stability(rootRef, accessor)));
        float retention = (float) Math.pow(stability / 100f, exec.getCastCount());
        float volMax = (tracker.getInitial() + CasterStats.volatility(rootRef, accessor))
                * ctx.policy().getTierScale();
        float startingBudget = Math.max(0f, volMax * retention);
        tracker.setCurrent(startingBudget);
        tracker.setInitial(startingBudget);
        exec.advanceCast();
    }
}
