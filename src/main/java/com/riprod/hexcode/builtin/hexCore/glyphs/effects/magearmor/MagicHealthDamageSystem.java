package com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.glyphs.effects.magearmor.component.MagicHealthComponent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.core.common.execution.component.HexStats;

public class MagicHealthDamageSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return MagicHealthComponent.getComponentType();
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
            float incoming = damage.getAmount();
            if (incoming <= 0f) return;

            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            HexStats stats = findMageArmorVolatility(buffer, ref);
            if (stats == null) return;

            float available = stats.getCurrentVolatility();
            if (available <= 0f) return;

            float absorbed = Math.min(incoming, available);
            float multiplier = stats.getVolatilityMultiplier();
            stats.consumeVolatility(multiplier > 0f ? absorbed / multiplier : absorbed);
            damage.setAmount(incoming - absorbed);
        } catch (Exception e) {
            LOGGER.atSevere().log("MagicHealthDamageSystem failed: %s", e.getMessage());
        }
    }

    @Nullable
    private HexStats findMageArmorVolatility(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref) {
        AtomicReference<HexStats> found = new AtomicReference<>();
        ConstructStateUtil.forEachStatus(buffer, ref, MageArmorGlyph.ID, (id, status) -> {
            if (found.get() == null) found.set(status.getHexContext().getHexStats());
        });
        return found.get();
    }
}
