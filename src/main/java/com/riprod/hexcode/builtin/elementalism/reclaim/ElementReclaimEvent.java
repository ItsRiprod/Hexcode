package com.riprod.hexcode.builtin.elementalism.reclaim;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ElementReclaimEvent extends EcsEvent {

    private final Ref<EntityStore> caster;
    private final String elementId;
    private final float amount;

    public ElementReclaimEvent(Ref<EntityStore> caster, String elementId, float amount) {
        this.caster = caster;
        this.elementId = elementId;
        this.amount = amount;
    }

    public Ref<EntityStore> getCaster() {
        return caster;
    }

    public String getElementId() {
        return elementId;
    }

    public float getAmount() {
        return amount;
    }
}
