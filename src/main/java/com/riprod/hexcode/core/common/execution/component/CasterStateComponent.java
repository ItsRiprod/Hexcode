package com.riprod.hexcode.core.common.execution.component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.CastEndEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.core.common.execution.cast.HexCast;
import com.riprod.hexcode.core.common.execution.cast.component.CastSlotComponent;
import com.riprod.hexcode.core.common.execution.cast.component.VolatilityComponent;
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
            if (cast == null) continue;
            var slot = cast.get(CastSlotComponent.TYPE);
            var key = slot != null ? slot.get() : null;
            if (key == null) n++;
        }
        return n;
    }

    public void evictOldest(@Nonnull HexCastStore casts, @Nonnull CommandBuffer<EntityStore> buffer) {
        for (int i = 0; i < activeCastIds.size(); i++) {
            UUID id = activeCastIds.get(i);
            HexCast cast = casts.get(id);
            if (cast == null) continue;
            var slot = cast.get(CastSlotComponent.TYPE);
            var key = slot != null ? slot.get() : null;
            if (key == null) {
                activeCastIds.remove(i);
                HexExecuter.endCast(cast, CastEndEvent.Reason.EVICTED, buffer);
                return;
            }
        }
    }

    public void fizzleSlot(@Nonnull HexCastStore casts, @Nonnull String slotKey,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        pruneCompleted(casts);
        for (UUID id : activeCastIds) {
            HexCast cast = casts.get(id);
            if (cast == null) continue;
            var slot = cast.get(CastSlotComponent.TYPE);
            var key = slot != null ? slot.get() : null;
            if (slotKey.equals(key) && cast.get(VolatilityComponent.TYPE).getCurrent() > 0f) {
                HexExecuter.endCast(cast, CastEndEvent.Reason.EVICTED, buffer);
            }
        }
    }

    public int cancelAll(@Nonnull HexCastStore casts, @Nonnull CommandBuffer<EntityStore> buffer) {
        int cancelled = 0;
        for (UUID id : new ArrayList<>(activeCastIds)) {
            HexCast cast = casts.get(id);
            if (cast == null || cast.get(VolatilityComponent.TYPE).getCurrent() <= 0f) continue;
            HexExecuter.endCast(cast, CastEndEvent.Reason.DISPELLED, buffer);
            cancelled++;
        }
        pruneCompleted(casts);
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
