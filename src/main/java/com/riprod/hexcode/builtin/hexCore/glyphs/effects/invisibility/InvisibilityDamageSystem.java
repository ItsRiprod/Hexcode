package com.riprod.hexcode.builtin.hexCore.glyphs.effects.invisibility;

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
import com.riprod.hexcode.core.common.construct.component.HexEffectsComponent;
import com.riprod.hexcode.core.common.construct.state.ConstructStateUtil;
import com.riprod.hexcode.utils.VfxUtil;

public class InvisibilityDamageSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public Query<EntityStore> getQuery() {
        return HexEffectsComponent.getComponentType();
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
            if (damage.getAmount() <= 0f) return;

            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            InvisibilityState state = ConstructStateUtil.findState(
                    buffer, ref, InvisibilityGlyph.TYPE, InvisibilityState.class);
            if (state == null) return;

            // stripping the native effect reveals immediately, the construct sees it gone
            // on its next tick and ends through the normal continuation path
            VfxUtil.removeEffect(buffer, ref, state.getEffectId());
        } catch (Exception e) {
            LOGGER.atSevere().log("InvisibilityDamageSystem failed: %s", e.getMessage());
        }
    }
}
