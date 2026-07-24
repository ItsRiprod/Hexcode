package com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.fortify.component.FortifyWardComponent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.glyphs.variables.EntityVar;
import com.riprod.hexcode.core.common.protection.HexProtection;

public class FortifyWardDamageSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return FortifyWardComponent.getComponentType();
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> buffer,
            @Nonnull Damage damage) {
        try {
            if (damage.isCancelled()) return;

            // the hex protection claim gate fires a zero-damage probe and reads its cancelled
            // state; consuming the ward here would falsely report the area as damage-disabled
            int probeCause = DamageCause.getAssetMap().getIndex(HexProtection.PROBE_CAUSE_ID);
            if (damage.getDamageCauseIndex() == probeCause) return;

            Ref<EntityStore> target = chunk.getReferenceTo(index);
            FortifyState state = ConstructStateUtil.findState(
                    buffer, target, FortifyGlyph.ID, FortifyState.class);
            if (state == null || state.isConsumed()) return;

            state.consume(resolveAttacker(damage, buffer));
            damage.setCancelled(true);
        } catch (Exception e) {
            LOGGER.atSevere().log("FortifyWardDamageSystem failed: %s", e.getMessage());
        }
    }

    @Nullable
    private static EntityVar resolveAttacker(Damage damage, CommandBuffer<EntityStore> buffer) {
        if (!(damage.getSource() instanceof Damage.EntitySource src)) return null;

        Ref<EntityStore> attacker = src.getRef();
        // orphaned projectiles fall back to the projectile entity as their own source
        if (src instanceof Damage.ProjectileSource projectile
                && attacker.equals(projectile.getProjectile())) {
            return null;
        }
        if (attacker == null || !attacker.isValid()) return null;

        UUIDComponent uuid = buffer.getComponent(attacker, UUIDComponent.getComponentType());
        if (uuid == null) return null;
        return new EntityVar(uuid.getUuid(), attacker);
    }
}
