package com.riprod.hexcode.core.common.execution.component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.resource.HexCastStore;

public class CasterStateComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, CasterStateComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, CasterStateComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, CasterStateComponent> getComponentType() {
        return componentType;
    }

    private List<UUID> activeCastIds = new ArrayList<>();

    public CasterStateComponent() {
    }

    public void registerActiveCast(@Nonnull HexCast cast) {
        activeCastIds.add(cast.getExecutionId());
    }

    public void pruneCompleted(@Nonnull HexCastStore casts) {
        activeCastIds.removeIf(id -> casts.get(id) == null);
    }

    public int getActiveCount(@Nonnull HexCastStore casts) {
        pruneCompleted(casts);
        int n = 0;
        for (UUID id : activeCastIds) {
            HexCast cast = casts.get(id);
            if (cast != null && cast.getSlotKey() == null) n++;
        }
        return n;
    }

    public void evictOldest(@Nonnull HexCastStore casts) {
        for (int i = 0; i < activeCastIds.size(); i++) {
            UUID id = activeCastIds.get(i);
            HexCast cast = casts.get(id);
            if (cast != null && cast.getSlotKey() == null) {
                activeCastIds.remove(i);
                cast.volatility().setCurrent(0f);
                casts.remove(id);
                return;
            }
        }
    }

    public void fizzleSlot(@Nonnull HexCastStore casts, @Nonnull String slotKey) {
        pruneCompleted(casts);
        for (UUID id : activeCastIds) {
            HexCast cast = casts.get(id);
            if (cast == null) continue;
            if (slotKey.equals(cast.getSlotKey()) && cast.volatility().getCurrent() > 0f) {
                cast.volatility().setCurrent(0f);
                casts.remove(id);
            }
        }
    }

    public int cancelAll(@Nonnull HexCastStore casts) {
        int cancelled = 0;
        for (UUID id : new ArrayList<>(activeCastIds)) {
            HexCast cast = casts.get(id);
            if (cast == null || cast.volatility().getCurrent() <= 0f) continue;
            cast.volatility().setCurrent(0f);
            cancelled++;
        }
        return cancelled;
    }

    @Nonnull
    public List<UUID> getActiveCastIds() {
        return activeCastIds;
    }

    @Nonnull
    @Override
    public CasterStateComponent clone() {
        CasterStateComponent copy = new CasterStateComponent();
        copy.activeCastIds = new ArrayList<>(this.activeCastIds);
        return copy;
    }
}
