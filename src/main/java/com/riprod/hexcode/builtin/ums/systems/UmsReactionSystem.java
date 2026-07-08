package com.riprod.hexcode.builtin.ums.systems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector4d;

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
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.ums.assets.ElementAsset;
import com.riprod.hexcode.builtin.ums.registry.UmsReactionContext;
import com.riprod.hexcode.builtin.ums.assets.BaseElementInteraction;

public class UmsReactionSystem extends DamageEventSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return EffectControllerComponent.getComponentType();
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

            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            EffectControllerComponent controller = buffer.getComponent(
                    ref, EffectControllerComponent.getComponentType());
            if (controller == null) return;

            int[] activeIndexes = controller.getActiveEffectIndexes();
            if (activeIndexes == null || activeIndexes.length == 0) return;

            DamageCause cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
            if (cause == null) return;
            String attackerCauseId = cause.getId();

            Vector3d hitPos = null;
            Vector4d hit = damage.getMetaStore().getMetaObject(Damage.HIT_LOCATION);
            if (hit != null) hitPos = new Vector3d(hit.x, hit.y, hit.z);

            for (int effectIndex : activeIndexes) {
                EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectIndex);
                if (effect == null) continue;
                ElementAsset element = ElementAsset.getAssetMap().getAsset(effect.getId());
                if (element == null) continue;
                BaseElementInteraction interaction = element.getInteraction(attackerCauseId);
                if (interaction == null) continue;
                interaction.apply(new UmsReactionContext(
                        buffer, ref, damage, attackerCauseId, element.getId(), hitPos));
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("UmsReactionSystem failed: %s", e.getMessage());
        }
    }
}
