package com.riprod.hexcode.core.common.execution.cast.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.execution.cast.HexComponentRegistry;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponent;
import com.riprod.hexcode.core.common.execution.cast.ecs.CastComponentType;

public final class ManaSourceComponent implements CastComponent {

    @Nullable
    private final Ref<EntityStore> entity;

    private ManaSourceComponent(@Nullable Ref<EntityStore> entity) {
        this.entity = entity;
    }

    @Nonnull
    public static ManaSourceComponent ofEntity(@Nonnull Ref<EntityStore> entity) {
        return new ManaSourceComponent(entity);
    }

    @Nonnull
    public static ManaSourceComponent free() {
        return new ManaSourceComponent(null);
    }

    @Nullable
    private EntityStatMap statMap(ComponentAccessor<EntityStore> accessor) {
        if (entity == null || !entity.isValid()) return null;
        return accessor.getComponent(entity, EntityStatMap.getComponentType());
    }

    public boolean tryConsume(float cost, ComponentAccessor<EntityStore> accessor) {
        if (entity == null) return true;
        if (cost <= 0)
            return true;
        EntityStatMap statMap = statMap(accessor);
        if (statMap == null)
            return false;
        int manaIndex = DefaultEntityStatTypes.getMana();
        if (statMap.get(manaIndex).get() < cost)
            return false;
        statMap.subtractStatValue(manaIndex, cost);
        return true;
    }

    public float getCurrent(ComponentAccessor<EntityStore> accessor) {
        if (entity == null) return Float.MAX_VALUE;
        EntityStatMap statMap = statMap(accessor);
        if (statMap == null)
            return 0f;
        return statMap.get(DefaultEntityStatTypes.getMana()).get();
    }

    public boolean add(float amount, ComponentAccessor<EntityStore> accessor) {
        if (entity == null) return false;
        if (amount <= 0)
            return false;
        EntityStatMap statMap = statMap(accessor);
        if (statMap == null)
            return false;
        int manaIndex = DefaultEntityStatTypes.getMana();
        float before = statMap.get(manaIndex).get();
        if (before >= statMap.get(manaIndex).getMax())
            return false;
        statMap.addStatValue(manaIndex, amount);
        return true;
    }

    @Nonnull
    @Override
    public ManaSourceComponent copy() {
        return new ManaSourceComponent(entity);
    }

    @Nonnull
    @Override
    public ManaSourceComponent branch() {
        return this;
    }

    public static final CastComponentType<ManaSourceComponent> TYPE = HexComponentRegistry.register(
            ManaSourceComponent.class, "ManaSource", a -> ManaSourceComponent.ofEntity(a.root()));
}
