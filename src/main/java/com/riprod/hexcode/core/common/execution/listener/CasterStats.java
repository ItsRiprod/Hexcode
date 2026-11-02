package com.riprod.hexcode.core.common.execution.listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.stats.HexcodeEntityStatTypes;

public final class CasterStats {

    private static final float DEFAULT_STABILITY = 100f;

    private CasterStats() {
    }

    @Nullable
    private static EntityStatMap statMap(@Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        return accessor.getComponent(ref, EntityStatMap.getComponentType());
    }

    public static float volatility(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
        EntityStatMap statMap = statMap(ref, accessor);
        if (statMap != null) {
            int volIndex = HexcodeEntityStatTypes.getVolatility();
            if (volIndex != Integer.MIN_VALUE) {
                EntityStatValue volStat = statMap.get(volIndex);
                if (volStat != null)
                    return volStat.getMax();
            }
        }
        return 0.0f;
    }

    public static float stability(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor) {
        EntityStatMap statMap = statMap(ref, accessor);
        if (statMap != null) {
            int idx = HexcodeEntityStatTypes.getStability();
            if (idx != Integer.MIN_VALUE) {
                EntityStatValue stat = statMap.get(idx);
                if (stat != null)
                    return stat.getMax();
            }
        }
        return DEFAULT_STABILITY;
    }

    public static float maxMagicCharges(@Nonnull Ref<EntityStore> ref,
            @Nonnull ComponentAccessor<EntityStore> accessor) {
        int chargesIndex = HexcodeEntityStatTypes.getMagicCharges();
        if (chargesIndex != Integer.MIN_VALUE) {
            EntityStatMap statMap = statMap(ref, accessor);
            if (statMap != null) {
                EntityStatValue chargesStat = statMap.get(chargesIndex);
                if (chargesStat != null) {
                    return chargesStat.getMax();
                }
            }
        }
        return 0.0f;
    }
}
