package com.riprod.hexcode.builtin.hexCore.glyphs.erode.system;

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
import com.riprod.hexcode.builtin.hexCore.glyphs.erode.ErodeGlyph;
import com.riprod.hexcode.builtin.hexCore.glyphs.erode.ErodeState;
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;

public class ErodeDamageSystem extends DamageEventSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return HexEffectsComponent.getComponentType();
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
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            ErodeState state = ConstructStateUtil.findState(
                    buffer, ref, ErodeGlyph.ID, ErodeState.class);
            if (state == null) return;

            float original = damage.getAmount();
            float amplified = original * (1.0f + state.getVulnerabilityMultiplier());
            damage.setAmount(amplified);
        } catch (Exception e) {
            LOGGER.atSevere().log("ErodeDamageSystem failed: %s", e.getMessage());
        }
    }
}
