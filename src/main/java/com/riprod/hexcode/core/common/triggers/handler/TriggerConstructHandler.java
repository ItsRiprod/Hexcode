package com.riprod.hexcode.core.common.triggers.handler;

import java.util.List;

import com.hypixel.hytale.logger.HytaleLogger;
import com.riprod.hexcode.core.common.construct.component.ConstructTickContext;
import com.riprod.hexcode.core.common.construct.component.HexStatus;
import com.riprod.hexcode.core.common.construct.handler.ConstructHandler;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;
import com.riprod.hexcode.core.common.triggers.state.TriggerState;

public class TriggerConstructHandler implements ConstructHandler<TriggerState> {

    public static final String HANDLER_ID = "Hexcode_Trigger";

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public boolean onTick(float dt, HexStatus<TriggerState> status, ConstructTickContext ctx) {
        TriggerState state = status.getState();
        if (state == null) return true;
        if (state.isFired()) return true;
        drainSustain(dt, status);
        return false;
    }

    @Override
    public void onEnd(HexStatus<TriggerState> status, ConstructTickContext ctx) {
        // resume already happened in HexResumeCallback; this path runs when the construct
        // self-ends after the fired flag is set on the state.
        unsubscribe(status);
        onCleanup(status, ctx);
    }

    @Override
    public void onAbort(HexStatus<TriggerState> status, ConstructTickContext ctx) {
        // construct died before its event fired (volatility depleted, kill requested, owner gone)
        unsubscribe(status);
        onCleanup(status, ctx);
    }

    @Override
    public List<String> getPendingNextGlyphIds(HexStatus<TriggerState> status) {
        TriggerState state = status.getState();
        return state != null ? state.getNextGlyphIds() : List.of();
    }

    @Override
    public void setPendingNextGlyphIds(HexStatus<TriggerState> status, List<String> ids) {
        TriggerState state = status.getState();
        if (state != null) state.setNextGlyphIds(ids);
    }

    private void unsubscribe(HexStatus<TriggerState> status) {
        TriggerState state = status.getState();
        if (state == null || state.getTriggerKey() == null || state.getSubscriptionId() == null) return;
        try {
            TriggerListenerRegistry registry = status.getHexContext().getAccessor()
                    .getResource(TriggerListenerRegistry.getResourceType());
            if (registry != null) {
                registry.unsubscribe(state.getTriggerKey(), state.getSubscriptionId());
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("trigger unsubscribe failed: %s", e.getMessage());
        }
    }
}
