package com.riprod.hexcode.api.event;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public class HexCastEvent extends CancellableEcsEvent {

    private final HexContext context;

    public HexCastEvent(HexContext context) {
        this.context = context;
    }

    public HexContext getContext() {
        return context;
    }

    public static final class Pre extends HexCastEvent {
        public Pre(HexContext context) {
            super(context);
        }
    }
}
