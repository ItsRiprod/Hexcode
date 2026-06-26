package com.riprod.hexcode.builtin.imbued.triggers.death;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.imbued.triggers.TriggerKey;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;

public class DeathTriggerSource extends DeathSystems.OnDeathSystem {

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer) {
        TriggerListenerRegistry registry = buffer.getResource(TriggerListenerRegistry.getResourceType());
        if (registry == null || registry.countListeners(TriggerKey.DEATH) == 0) return;

        UUIDComponent uuidComp = buffer.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComp == null) return;

        Ref<EntityStore> killer = null;
        Damage damage = component.getDeathInfo();
        if (damage != null && damage.getSource() instanceof Damage.EntitySource es) {
            Ref<EntityStore> k = es.getRef();
            if (k != null && k.isValid()) killer = k;
        }
        DeathPayload payload = new DeathPayload(ref, killer);
        registry.fire(buffer, new TriggerEvent(TriggerKey.DEATH, uuidComp.getUuid(), ref, payload));
    }
}
