package com.riprod.hexcode.builtin.hexCore.glyphs.effects.domain;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class DomainAuraState implements ConstructState {

    private Ref<EntityStore> zoneRef;
    private float volatilityBoost;

    public DomainAuraState() {
    }

    public DomainAuraState(Ref<EntityStore> zoneRef, float volatilityBoost) {
        this.zoneRef = zoneRef;
        this.volatilityBoost = volatilityBoost;
    }

    public Ref<EntityStore> getZoneRef() {
        return zoneRef;
    }

    public float getVolatilityBoost() {
        return volatilityBoost;
    }

    @Override
    public DomainAuraState copy() {
        return new DomainAuraState(zoneRef, volatilityBoost);
    }
}
