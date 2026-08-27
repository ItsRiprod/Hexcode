package com.riprod.hexcode.core.common.execution.system;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.HexCastEvent;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
import com.riprod.hexcode.core.common.execution.root.PlayerHexRoot;

public class CastSpellPowerSystem extends WorldEventSystem<EntityStore, HexCastEvent.Pre> {

    public CastSpellPowerSystem() {
        super(HexCastEvent.Pre.class);
    }

    @Override
    public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull HexCastEvent.Pre event) {
        if (event.isCancelled()) return;
        HexContext context = event.getContext();
        if (!(context.getHexRoot() instanceof PlayerHexRoot playerRoot)) return;
        VolatilityComponent tracker = context.volatility();
        if (tracker == null) return;
        tracker.setComplexityMultiplier(playerRoot.resolveSpellPower(buffer));
    }
}
