package com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.magearmor.component.MagicHealthComponent;

public class MagicHealthDamageSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return MagicHealthComponent.getComponentType();
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getInspectDamageGroup();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull Damage damage) {
        try {
            if (damage.isCancelled()) return;
            float incoming = damage.getAmount();
            if (incoming <= 0f) return;

            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            EntityStatMap statMap = buffer.getComponent(ref, EntityStatMap.getComponentType());
            if (statMap == null) return;

            int statIndex = EntityStatType.getAssetMap().getIndex(MagicHealthComponent.STAT_ID);
            if (statIndex == Integer.MIN_VALUE) return;
            EntityStatValue pool = statMap.get(statIndex);
            if (pool == null) return;

            float available = pool.get();
            if (available <= 0f) {
                deplete(ref, buffer);
                return;
            }

            float absorbed = Math.min(incoming, available);
            statMap.subtractStatValue(statIndex, absorbed);
            damage.setAmount(incoming - absorbed);

            if (available - absorbed <= 0f) {
                deplete(ref, buffer);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("MagicHealthDamageSystem failed: %s", e.getMessage());
        }
    }

    private void deplete(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer) {
        MagicHealthComponent tracking = buffer.getComponent(ref, MagicHealthComponent.getComponentType());
        String effectId = tracking != null ? tracking.getEffectId() : null;
        if (effectId != null) {
            EffectControllerComponent controller = buffer.getComponent(
                    ref, EffectControllerComponent.getComponentType());
            if (controller != null) {
                int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
                if (effectIndex != Integer.MIN_VALUE) {
                    controller.removeEffect(ref, effectIndex, buffer);
                }
            }
        }
        buffer.removeComponent(ref, MagicHealthComponent.getComponentType());
    }
}
