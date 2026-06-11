package com.riprod.hexcode.builtin.triggers.sources;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.triggers.AttackedTrigger;
import com.riprod.hexcode.builtin.triggers.BlockTrigger;
import com.riprod.hexcode.builtin.triggers.OnAttackTrigger;
import com.riprod.hexcode.builtin.triggers.OnShootTrigger;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedArmorMarker;
import com.riprod.hexcode.core.common.imbuement.component.ImbuedHotbarMarker;
import com.riprod.hexcode.core.common.imbuement.payload.EntityDamagedPayload;
import com.riprod.hexcode.core.common.imbuement.payload.EntityHitPayload;
import com.riprod.hexcode.core.common.triggers.component.TriggerEvent;
import com.riprod.hexcode.core.common.triggers.registry.TriggerListenerRegistry;

public final class EntityHitEventSource {

    private EntityHitEventSource() {
    }

    public static final class OnDamageDealtSystem extends DamageEventSystem {

        @Nullable
        @Override
        public SystemGroup<EntityStore> getGroup() {
            return DamageModule.get().getInspectDamageGroup();
        }

        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return Archetype.empty();
        }

        public void handle(int index,
                @Nonnull ArchetypeChunk<EntityStore> chunk,
                @Nonnull Store<EntityStore> store,
                @Nonnull CommandBuffer<EntityStore> buffer,
                @Nonnull Damage damage) {
            if (damage.getAmount() <= 0f) return;
            if (!(damage.getSource() instanceof Damage.EntitySource entSrc)) return;
            Ref<EntityStore> attacker = entSrc.getRef();
            if (attacker == null || !attacker.isValid()) return;

            ImbuedHotbarMarker marker = buffer.getComponent(attacker, ImbuedHotbarMarker.getComponentType());
            if (marker == null) return;

            String triggerId = (entSrc instanceof Damage.ProjectileSource)
                    ? OnShootTrigger.ID : OnAttackTrigger.ID;

            TriggerListenerRegistry registry = buffer.getResource(TriggerListenerRegistry.getResourceType());
            if (registry == null || registry.countListeners(triggerId) == 0) return;

            UUIDComponent attackerUuidComp = buffer.getComponent(attacker, UUIDComponent.getComponentType());
            if (attackerUuidComp == null) return;

            Ref<EntityStore> target = chunk.getReferenceTo(index);
            UUIDComponent targetUuidComp = buffer.getComponent(target, UUIDComponent.getComponentType());

            EntityHitPayload payload = new EntityHitPayload(target, targetUuidComp != null ? targetUuidComp.getUuid() : null);
            registry.fire(buffer, new TriggerEvent(triggerId, attackerUuidComp.getUuid(), attacker, payload));
        }
    }

    public static final class OnDamageReceivedSystem extends DamageEventSystem {

        private final Query<EntityStore> query = ImbuedArmorMarker.getComponentType();

        @Nullable
        @Override
        public SystemGroup<EntityStore> getGroup() {
            return DamageModule.get().getInspectDamageGroup();
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return query;
        }

        public void handle(int index,
                @Nonnull ArchetypeChunk<EntityStore> chunk,
                @Nonnull Store<EntityStore> store,
                @Nonnull CommandBuffer<EntityStore> buffer,
                @Nonnull Damage damage) {
            if (damage.getAmount() <= 0f) return;
            if (!(damage.getSource() instanceof Damage.EntitySource entSrc)) return;

            Ref<EntityStore> target = chunk.getReferenceTo(index);
            Ref<EntityStore> attacker = entSrc.getRef();

            Boolean blocked = damage.getMetaObject(Damage.BLOCKED);
            String triggerId;
            if (Boolean.TRUE.equals(blocked)) {
                ImbuedHotbarMarker held = buffer.getComponent(target, ImbuedHotbarMarker.getComponentType());
                if (held == null) return;
                triggerId = BlockTrigger.ID;
            } else {
                triggerId = AttackedTrigger.ID;
            }

            TriggerListenerRegistry registry = buffer.getResource(TriggerListenerRegistry.getResourceType());
            if (registry == null || registry.countListeners(triggerId) == 0) return;

            UUIDComponent targetUuidComp = buffer.getComponent(target, UUIDComponent.getComponentType());
            if (targetUuidComp == null) return;

            UUID attackerUuid = null;
            if (attacker != null && attacker.isValid()) {
                UUIDComponent attackerUuidComp = buffer.getComponent(attacker, UUIDComponent.getComponentType());
                if (attackerUuidComp != null) attackerUuid = attackerUuidComp.getUuid();
            }

            EntityDamagedPayload payload = new EntityDamagedPayload(attacker, attackerUuid);
            registry.fire(buffer, new TriggerEvent(triggerId, targetUuidComp.getUuid(), target, payload));
        }
    }
}
